package com.assistant.device

import android.Manifest
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioFormat
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.CalendarContract
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

enum class AssistantState {
    SLEEP,
    ACTIVE
}

data class PendingAction(
    val type: String,
    val contactOrTitle: String,
    val messageOrTime: String
)

class BluetoothService : Service(), TextToSpeech.OnInitListener {
    private val TAG = "BluetoothService"
    private var pendingSafetyAction: PendingAction? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var connectionJob: Job? = null
    private var ioJob: Job? = null

    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = false
    private lateinit var database: AssistantDatabase
    private var geminiClient: GeminiClient? = null
    private var ollamaClient: OllamaClient? = null
    
    // Audio recording accumulator (raw PCM 16kHz Mono 16-bit)
    private val audioRecordingBuffer = ArrayList<Byte>()
    private var lastAudioPacketTime = 0L
    private var audioTimeoutJob: Job? = null

    // Background Speech Recognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningLoopRunning = false
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    companion object {
        val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)

        // Live Voice assistant flows observed by UI
        val lastTranscribedQuery = MutableStateFlow<String>("")
        val lastAssistantResponse = MutableStateFlow<String>("")
        val assistantSpeechState = MutableStateFlow<String>("Tap Mic to speak")
        val isListeningActive = MutableStateFlow<Boolean>(false)
        val assistantMode = MutableStateFlow<AssistantState>(AssistantState.ACTIVE)
        val totalBytesTransferred = MutableStateFlow<Long>(0L)
        val streamLatencyMs = MutableStateFlow<Long>(12L)

        const val CHANNEL_ID = "BluetoothServiceChannel"
        const val NOTIFICATION_ID = 101
        
        const val ACTION_EVALUATE_BUZZ = "com.assistant.device.action.EVALUATE_BUZZ"
        const val ACTION_START_LISTENING = "com.assistant.device.action.START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.assistant.device.action.STOP_LISTENING"
        const val ACTION_TOGGLE_LISTENING = "com.assistant.device.action.TOGGLE_LISTENING"
        const val ACTION_RELOAD_CONFIG = "com.assistant.device.action.RELOAD_CONFIG"
        
        // Protocol Headers (matching ESP32)
        const val PKT_AUDIO_MIC: Byte = 0x01
        const val PKT_AUDIO_SPK: Byte = 0x02
        const val PKT_START_BUZZ: Byte = 0x03
        const val PKT_STOP_BUZZ: Byte = 0x04
        const val PKT_PING: Byte = 0x05

        // SPP UUID for Bluetooth Classic Serial Port
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    override fun onCreate() {
        super.onCreate()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        database = AssistantDatabase.getDatabase(applicationContext)
        tts = TextToSpeech(this, this)
        
        initializeLlmClients()

        createNotificationChannel()
        
        refreshForegroundService()

        // Start Bluetooth connection loop
        startConnectionLoop()

        // Start continuous active listening loop if permissions are already granted
        startContinuousSpeechRecognizer()
    }

    private fun initializeLlmClients() {
        val sharedPrefs = getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE)
        val provider = sharedPrefs.getString("ai_provider", "gemini") ?: "gemini"
        
        // Reset existing clients
        geminiClient = null
        ollamaClient = null

        var geminiKey = sharedPrefs.getString("gemini_api_key", "") ?: ""
        if (geminiKey.isEmpty()) {
            geminiKey = "AQ.Ab8RN6LX_reZkhcO8CQv02-6fOEU8lMEMkP5VTOaqKkwVJWW6A"
            sharedPrefs.edit().putString("gemini_api_key", geminiKey).apply()
        }
        geminiClient = GeminiClient(applicationContext, geminiKey)

        if (provider == "ollama") {
            var endpoint = sharedPrefs.getString("ollama_endpoint", "https://ollama.com") ?: "https://ollama.com"
            if (endpoint.contains("localhost") || endpoint.contains("127.0.0.1") || endpoint.contains("10.0.2.2") || endpoint.contains("ollama.com/api")) {
                endpoint = "https://ollama.com"
            }
            var model = sharedPrefs.getString("ollama_model", "gpt-oss:120b-cloud") ?: "gpt-oss:120b-cloud"
            if (model == "llama3" || model == "gpt-oss:120b") {
                model = "gpt-oss:120b-cloud"
            }
            var apiKey = sharedPrefs.getString("ollama_api_key", "206cf8d5978245ca97f529cb37d20545") ?: "206cf8d5978245ca97f529cb37d20545"
            if (apiKey.isEmpty()) {
                apiKey = "206cf8d5978245ca97f529cb37d20545"
            }
            ollamaClient = OllamaClient(applicationContext, endpoint, model, apiKey)
        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refreshForegroundService() // Ensure types are updated if permissions were just granted
        when (intent?.action) {
            ACTION_EVALUATE_BUZZ -> evaluateBuzzStatus()
            ACTION_START_LISTENING -> startContinuousSpeechRecognizer()
            ACTION_STOP_LISTENING -> stopContinuousSpeechRecognizer()
            ACTION_TOGGLE_LISTENING -> toggleListeningState()
            ACTION_RELOAD_CONFIG -> initializeLlmClients()
        }
        return START_STICKY
    }

    private fun refreshForegroundService(statusText: String = "Waiting for hardware connection...") {
        val notification = getNotification(statusText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val hasBluetooth = checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val hasMicrophone = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            
            var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC // Safe fallback
            if (hasBluetooth) type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            if (hasMicrophone) type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            
            startForeground(NOTIFICATION_ID, notification, type)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
            isTtsInitialized = true
            
            // Register Speech completion listener
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    assistantSpeechState.value = "Speaking..."
                }
                override fun onDone(utteranceId: String?) {
                    assistantSpeechState.value = "Tap Mic to speak"
                    // Delete temp TTS audio files after streaming is done
                    utteranceId?.let {
                        val file = File(externalCacheDir, "$it.wav")
                        if (file.exists()) file.delete()
                    }
                }
                override fun onError(utteranceId: String?) {
                    assistantSpeechState.value = "Tap Mic to speak"
                }
            })
        }
    }

    private fun stopContinuousSpeechRecognizer() {
        isListeningLoopRunning = false
        isListeningActive.value = false
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (e: Exception) {}
            speechRecognizer = null
            assistantSpeechState.value = "Microphone Off (Tap Mic to start)"
        }
    }

    private fun toggleListeningState() {
        if (isListeningActive.value) {
            stopContinuousSpeechRecognizer()
        } else {
            startContinuousSpeechRecognizer()
        }
    }

    private fun startContinuousSpeechRecognizer() {
        val connected = (connectionState.value == ConnectionState.CONNECTED)
        isListeningLoopRunning = !connected
        isListeningActive.value = true

        mainHandler.post {
            if (speechRecognizer != null) {
                try {
                    speechRecognizer?.destroy()
                } catch (e: Exception) {
                    Log.e(TAG, "Error destroying speech recognizer", e)
                }
                speechRecognizer = null
            }

            if (!connected && !SpeechRecognizer.isRecognitionAvailable(this)) {
                Log.e(TAG, "Speech recognition not available on this device.")
                assistantSpeechState.value = "Speech recognition unavailable"
                return@post
            }

            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Microphone recording permission not granted yet. Skipping listening loop.")
                assistantSpeechState.value = "Microphone Permission Required"
                return@post
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        assistantSpeechState.value = "Listening..."
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        assistantSpeechState.value = "Processing..."
                    }

                    override fun onError(error: Int) {
                        Log.d(TAG, "SpeechRecognizer error: $error")
                        assistantSpeechState.value = if (connected) "Hardware Connected - Mic Active" else "Tap Mic to speak"
                        scheduleSpeechRecognizerRestart()
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val userText = matches[0]
                            lastTranscribedQuery.value = userText
                            processLocalVoiceCommand(userText)
                        } else {
                            scheduleSpeechRecognizerRestart()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }

            try {
                speechRecognizer?.startListening(recognizerIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognizer", e)
                scheduleSpeechRecognizerRestart()
            }
        }
    }

    private fun pausePhoneMicrophone() {
        isListeningLoopRunning = false
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.d(TAG, "Error pausing speech recognizer: ${e.message}")
            }
            speechRecognizer = null
            assistantSpeechState.value = "Hardware Connected - Mic Active"
        }
    }

    private fun resumePhoneMicrophone() {
        if (!isListeningLoopRunning) {
            isListeningLoopRunning = true
            startContinuousSpeechRecognizer()
        }
    }

    private fun scheduleSpeechRecognizerRestart() {
        mainHandler.postDelayed({
            if (isListeningLoopRunning) {
                startContinuousSpeechRecognizer()
            }
        }, 800)
    }

    // --- Action Handler for Voice Commands ---

    private fun processLocalVoiceCommand(command: String) {
        val query = command.lowercase(Locale.getDefault()).trim()
        Log.i(TAG, "Executing voice assistant command: $query (Current mode: ${assistantMode.value})")

        // 1. WAKE WORD DETECTION (homehub activate / homehub wake up / wake up homehub)
        val isWakePhrase = query.contains("homehub activate") || 
                           query.contains("homehub wake up") || 
                           query.contains("wake up homehub") || 
                           query.contains("hey homehub") || 
                           query.contains("homehub wake") || 
                           query.contains("activate homehub")

        if (isWakePhrase) {
            assistantMode.value = AssistantState.ACTIVE
            isListeningActive.value = true
            
            // Reset LLM Chat History for a fresh session upon wake-up
            geminiClient?.resetChatSession()
            ollamaClient?.resetChatSession()
            
            speakToDevice("HomeHub activated and ready. How can I help you?")
            scheduleSpeechRecognizerRestart()
            return
        }


        // 2. SLEEP WORD DETECTION (homehub go to sleep / go to sleep / homehub sleep)
        val isSleepPhrase = query.contains("homehub go to sleep") || 
                            query.contains("go to sleep") || 
                            query.contains("homehub sleep") || 
                            query.contains("homehub deactivate") || 
                            query.contains("sleep homehub") || 
                            query.contains("deactivate homehub")

        if (isSleepPhrase) {
            assistantSpeechState.value = "Going to sleep..."
            serviceScope.launch {
                try {
                    val sleepMessage = if (geminiClient != null) {
                        geminiClient!!.getResponse("User said '$command'. Answer warmly in 1 short sentence that you are going to sleep now and to say 'HomeHub wake up' whenever they need you.")
                    } else {
                        "Going to sleep now. Say 'HomeHub wake up' whenever you need me."
                    }
                    speakToDevice(sleepMessage)
                } catch (e: Exception) {
                    speakToDevice("Going to sleep now. Say 'HomeHub wake up' whenever you need me.")
                } finally {
                    assistantMode.value = AssistantState.SLEEP
                    stopContinuousSpeechRecognizer()
                }
            }
            return
        }

        // 3. IF ASLEEP AND NOT A WAKE WORD -> IGNORE BACKGROUND NOISE
        if (assistantMode.value == AssistantState.SLEEP) {
            Log.d(TAG, "Ignoring spoken audio because HomeHub is asleep: $query")
            scheduleSpeechRecognizerRestart()
            return
        }

        // 4. DETECT VOICE CONFIRMATION FOR PENDING ACTION SAFETY GUARD
        if (pendingSafetyAction != null) {
            if (query.contains("yes") || query.contains("confirm") || query.contains("send") || query.contains("do it") || query.contains("go ahead")) {
                val action = pendingSafetyAction!!
                pendingSafetyAction = null
                if (action.type == "whatsapp") {
                    launchWhatsApp()
                    speakToDevice("Action Safety Guard: Confirmed! Opening WhatsApp to send '${action.messageOrTime}' to ${action.contactOrTitle}.")
                } else {
                    launchCalendar()
                    speakToDevice("Action Safety Guard: Confirmed! Opening Calendar for '${action.contactOrTitle}'.")
                }
                scheduleSpeechRecognizerRestart()
                return
            } else if (query.contains("no") || query.contains("cancel") || query.contains("stop") || query.contains("don't")) {
                pendingSafetyAction = null
                speakToDevice("Action cancelled.")
                scheduleSpeechRecognizerRestart()
                return
            }
        }

        // 5. INTERCEPT EXPLICIT MESSAGE COMMANDS FOR SAFETY GUARD
        if ((query.contains("send") && query.contains("message")) || query.contains("whatsapp to")) {
            val contact = if (query.contains("to ")) query.substringAfter("to ").substringBefore(" ").replaceFirstChar { it.uppercase() } else "Contact"
            val msgText = if (query.contains("saying ")) query.substringAfter("saying ") else "Hello"
            pendingSafetyAction = PendingAction("whatsapp", contact, msgText)
            speakToDevice("Action Safety Guard: Ready to send '$msgText' to $contact on WhatsApp. Say YES to confirm or NO to cancel.")
            scheduleSpeechRecognizerRestart()
            return
        }

        // 1. Music Actions
        if (query.contains("play music") || query.contains("resume music") || query.contains("play song") || query.contains("resume")) {
            executeMusicAction(KeyEvent.KEYCODE_MEDIA_PLAY)
            speakToDevice("Playing music.")
            scheduleSpeechRecognizerRestart()
            return
        }
        if (query.contains("pause music") || query.contains("stop music") || query.contains("pause") || query.contains("stop")) {
            executeMusicAction(KeyEvent.KEYCODE_MEDIA_PAUSE)
            speakToDevice("Music paused.")
            scheduleSpeechRecognizerRestart()
            return
        }
        if (query.contains("next song") || query.contains("skip") || query.contains("next")) {
            executeMusicAction(KeyEvent.KEYCODE_MEDIA_NEXT)
            speakToDevice("Skipping song.")
            scheduleSpeechRecognizerRestart()
            return
        }
        if (query.contains("previous song") || query.contains("go back") || query.contains("previous")) {
            executeMusicAction(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            speakToDevice("Playing previous song.")
            scheduleSpeechRecognizerRestart()
            return
        }

        // 2. Calendar & Utilities
        if (query.contains("open calendar") || query.contains("calendar") || query.contains("schedule")) {
            launchCalendar()
            speakToDevice("Opening Calendar.")
            scheduleSpeechRecognizerRestart()
            return
        }
        if (query.contains("open maps") || query.contains("maps") || query.contains("navigate")) {
            launchMaps()
            speakToDevice("Opening Maps.")
            scheduleSpeechRecognizerRestart()
            return
        }

        // 4. Default LLM Fallback (Gemini / Ollama)
        val sharedPrefs = getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE)
        val provider = sharedPrefs.getString("ai_provider", "gemini") ?: "gemini"

        if (provider == "ollama") {
            if (ollamaClient == null) {
                speakToDevice("Please configure your Ollama settings in the mobile app.")
                scheduleSpeechRecognizerRestart()
                return
            }
            assistantSpeechState.value = "Thinking..."
            serviceScope.launch {
                try {
                    Log.d(TAG, "Sending query to Ollama: $command")
                    val response = ollamaClient!!.getResponse(command)
                    Log.d(TAG, "Received response from Ollama: ${response.take(100)}...")
                    speakToDevice(response)
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating Ollama response", e)
                    speakToDevice("Sorry, I had trouble processing that request with local brain. ${e.message}")
                } finally {
                    scheduleSpeechRecognizerRestart()
                }
            }
        } else {
            if (geminiClient == null) {
                speakToDevice("Please configure your Gemini API key in the mobile app.")
                scheduleSpeechRecognizerRestart()
                return
            }
            assistantSpeechState.value = "Thinking..."
            serviceScope.launch {
                try {
                    Log.d(TAG, "Sending query to Gemini: $command")
                    var isFirstChunk = true
                    val fullResponse = geminiClient!!.getResponseStream(command) { sentenceChunk ->
                        if (isFirstChunk) {
                            assistantSpeechState.value = "Speaking..."
                            speakToDevice(sentenceChunk, append = false)
                            isFirstChunk = false
                        } else {
                            speakToDevice(sentenceChunk, append = true)
                        }
                    }
                    Log.d(TAG, "Received full streaming response from Gemini: ${fullResponse.take(100)}...")
                    lastAssistantResponse.value = fullResponse

                    database.voiceChatDao().insert(
                        VoiceChatEntity(
                            timestamp = System.currentTimeMillis(),
                            userQuery = command,
                            assistantResponse = fullResponse,
                            provider = "Gemini Cloud"
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating Gemini response", e)
                    speakToDevice("Sorry, I had trouble processing that request. Error: ${e.message}")
                } finally {
                    scheduleSpeechRecognizerRestart()
                }
            }
        }
    }

    private fun executeMusicAction(keyCode: Int) {
        val actionStr = when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> "play"
            KeyEvent.KEYCODE_MEDIA_PAUSE -> "pause"
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "toggle"
            KeyEvent.KEYCODE_MEDIA_NEXT -> "next"
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "previous"
            KeyEvent.KEYCODE_MEDIA_STOP -> "stop"
            else -> "toggle"
        }

        // Try official MediaSessionManager transportControls first
        val mediaIntegration = com.assistant.device.integrations.MediaIntegration(applicationContext)
        val handled = mediaIntegration.executeMediaControl(actionStr)

        if (!handled) {
            val sharedPrefs = getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE)
            val preferredApp = sharedPrefs.getString("preferred_music_app", "com.google.android.apps.youtube.music") ?: "com.google.android.apps.youtube.music"

            if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                try {
                    val launchIntent = packageManager.getLaunchIntentForPackage(preferredApp)?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    if (launchIntent != null) {
                        startActivity(launchIntent)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Music app launch skipped: ${e.message}")
                }
            }

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
    }

    private fun launchWhatsApp() {
        val intent = packageManager.getLaunchIntentForPackage("com.whatsapp")?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent != null) {
            startActivity(intent)
        } else {
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.whatsapp")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                startActivity(playStoreIntent)
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(browserIntent)
            }
        }
    }

    private fun launchCalendar() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setData(CalendarContract.CONTENT_URI)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch calendar intent", e)
        }
    }

    private fun launchMaps() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=location")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch maps intent", e)
        }
    }

    // --- End Continuous Voice recognizer Loop ---

    private fun startConnectionLoop() {
        connectionJob?.cancel()
        connectionJob = serviceScope.launch {
            while (isActive) {
                if (bluetoothSocket == null || !bluetoothSocket!!.isConnected) {
                    connectionState.value = ConnectionState.CONNECTING
                    Log.d(TAG, "Attempting to connect to ESP32 virtual assistant...")
                    updateNotification("Connecting to virtual assistant...")
                    
                    val device = findAssistantDevice()
                    if (device != null) {
                        try {
                            // Check for BLUETOOTH_SCAN for cancelDiscovery
                            val hasScan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                            } else true

                            if (hasScan && bluetoothAdapter?.isDiscovering == true) {
                                bluetoothAdapter?.cancelDiscovery()
                            }
                            
                            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                            socket.connect()
                            bluetoothSocket = socket
                            
                            connectionState.value = ConnectionState.CONNECTED
                            Log.i(TAG, "Connected to ESP32 Virtual Assistant successfully!")
                            updateNotification("Connected to assistant hardware")
                            
                            pausePhoneMicrophone()
                            startIoListening(socket.inputStream, socket.outputStream)
                            sendPacket(PKT_PING)
                            
                        } catch (e: Exception) {
                            connectionState.value = ConnectionState.DISCONNECTED
                            Log.e(TAG, "Connection failed, retrying in 10s...", e)
                            bluetoothSocket = null
                            updateNotification("Hardware disconnected. Retrying...")
                            resumePhoneMicrophone()
                        }
                    } else {
                        connectionState.value = ConnectionState.DISCONNECTED
                        Log.w(TAG, "Virtual assistant hardware device not paired. Please pair 'HomeHub'.")
                        updateNotification("Pair 'HomeHub' to start.")
                        resumePhoneMicrophone()
                    }
                } else {
                    connectionState.value = ConnectionState.CONNECTED
                }
                delay(10000)
            }
        }
    }

    private fun findAssistantDevice(): BluetoothDevice? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission not granted. Cannot find paired devices.")
            return null
        }
        
        val pairedDevices = bluetoothAdapter?.bondedDevices ?: return null
        for (device in pairedDevices) {
            try {
                val name = device.name
                if (name == "HomeHub" || name == "TrueVirtualAssistant" || name == "ESP32-S3-Pod") {
                    return device
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException while accessing device name: ${e.message}")
            }
        }
        return null
    }

    private fun startIoListening(inputStream: InputStream, outputStream: OutputStream) {
        ioJob?.cancel()
        ioJob = serviceScope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val header = inputStream.read()
                    if (header == -1) break
                    
                    when (header.toByte()) {
                        PKT_STOP_BUZZ -> {
                            Log.i(TAG, "Stop buzz request received from device.")
                            tts.stop()
                            assistantSpeechState.value = "Tap Mic to speak"
                        }
                        
                        PKT_AUDIO_MIC -> {
                            val lenHigh = inputStream.read()
                            val lenLow = inputStream.read()
                            if (lenHigh == -1 || lenLow == -1) break
                            val len = (lenHigh shl 8) or lenLow
                            
                            val pcmBytes = ByteArray(len)
                            var readBytes = 0
                            while (readBytes < len) {
                                val read = inputStream.read(pcmBytes, readBytes, len - readBytes)
                                if (read == -1) break
                                readBytes += read
                            }
                            
                            if (readBytes == len) {
                                accumulateAudioData(pcmBytes)
                            }
                        }
                        
                        PKT_PING -> {
                            sendPacket(PKT_PING)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "I/O stream read error", e)
            } finally {
                Log.w(TAG, "I/O session ended. Re-triggering connection loop.")
                closeSocket()
            }
        }
    }

    private fun accumulateAudioData(data: ByteArray) {
        lastAudioPacketTime = System.currentTimeMillis()
        for (b in data) {
            audioRecordingBuffer.add(b)
        }

        audioTimeoutJob?.cancel()
        audioTimeoutJob = serviceScope.launch {
            delay(1200)
            processRecordedVoiceQuery()
        }
    }

    private fun processRecordedVoiceQuery() {
        if (audioRecordingBuffer.isEmpty()) return
        
        Log.i(TAG, "Audio recording finished. Processing query size: ${audioRecordingBuffer.size} bytes")
        
        val pcmData = audioRecordingBuffer.toByteArray()
        audioRecordingBuffer.clear()

        serviceScope.launch {
            val sharedPrefs = getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE)
            val provider = sharedPrefs.getString("ai_provider", "gemini") ?: "gemini"

            val wavFile = File(externalCacheDir, "user_query.wav")
            writePcmToWav(pcmData, wavFile)

            speakToDevice("Thinking...")

            try {
                val wavBytes = wavFile.readBytes()
                var responseText = ""

                if (geminiClient == null) {
                    speakToDevice("Please configure your Gemini API key in the mobile app.")
                    return@launch
                }

                val transcription = geminiClient!!.transcribeAudio(wavBytes)
                if (transcription.isEmpty()) {
                    speakToDevice("I couldn't transcribe the speech from the device audio.")
                    return@launch
                }

                Log.d(TAG, "Transcribed voice query: $transcription")
                lastTranscribedQuery.value = transcription

                if (provider == "ollama") {
                    if (ollamaClient == null) {
                        speakToDevice("Please configure your Ollama settings in the mobile app.")
                        return@launch
                    }
                    responseText = ollamaClient!!.getResponse(transcription)
                } else {
                    responseText = geminiClient!!.getResponse(transcription)
                }

                speakToDevice(responseText)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response for voice query", e)
                speakToDevice("Sorry, I had trouble processing that request.")
            } finally {
                if (wavFile.exists()) wavFile.delete()
            }
        }
    }

    private fun speakToDevice(text: String, append: Boolean = false) {
        if (!isTtsInitialized) {
            Log.e(TAG, "TTS not initialized!")
            return
        }

        Log.i(TAG, "Speaking: $text")

        // Request audio focus to duck background audio (music/podcasts)
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        } catch (e: Exception) {
            Log.d(TAG, "Audio focus request skipped: ${e.message}")
        }

        // 1. Speak out loud directly through phone speaker
        lastAssistantResponse.value = text
        val queueMode = if (append) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
        tts.speak(text, queueMode, null, "local_tts_utterance")

        // 2. Synthesize to WAV and stream to ESP32 over Bluetooth if connected
        val socket = bluetoothSocket
        if (socket != null && socket.isConnected) {
            val utteranceId = "tts_${System.currentTimeMillis()}"
            val outputFile = File(externalCacheDir, "$utteranceId.wav")
            val params = android.os.Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            val result = tts.synthesizeToFile(text, params, outputFile, utteranceId)
            if (result == TextToSpeech.SUCCESS) {
                serviceScope.launch(Dispatchers.IO) {
                    delay(500)
                    var prevSize = -1L
                    while (outputFile.exists() && outputFile.length() != prevSize) {
                        prevSize = outputFile.length()
                        delay(100)
                    }
                    
                    if (outputFile.exists() && outputFile.length() > 44) {
                        val inputStream = FileInputStream(outputFile)
                        inputStream.skip(44)
                        
                        val buffer = ByteArray(1024)
                        var bytesRead = inputStream.read(buffer)
                        while (bytesRead != -1 && bluetoothSocket?.isConnected == true) {
                            val socketOut = bluetoothSocket!!.outputStream
                            synchronized(socketOut) {
                                socketOut.write(PKT_AUDIO_SPK.toInt())
                                socketOut.write((bytesRead shr 8) and 0xFF)
                                socketOut.write(bytesRead and 0xFF)
                                socketOut.write(buffer, 0, bytesRead)
                            }
                            bytesRead = inputStream.read(buffer)
                            delay(25)
                        }
                        inputStream.close()
                    }
                }
            }
        }
    }

    private fun evaluateBuzzStatus() {
        serviceScope.launch {
            if (isUserInMeeting()) {
                Log.d(TAG, "User is in a meeting. Will not buzz.")
                sendPacket(PKT_STOP_BUZZ)
                return@launch
            }

            val sinceTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            val recentNotifications = database.notificationDao().getUnreadLast24Hours(sinceTime)
            
            if (recentNotifications.isNotEmpty()) {
                Log.d(TAG, "Unread notifications present. Sending PKT_START_BUZZ to device.")
                sendPacket(PKT_START_BUZZ)
            } else {
                Log.d(TAG, "No unread notifications. Ensuring buzzer is stopped.")
                sendPacket(PKT_STOP_BUZZ)
            }
        }
    }

    private fun isUserInMeeting(): Boolean {
        val now = System.currentTimeMillis()
        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.EVENT_LOCATION
        )
        
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, now - 5 * 60 * 1000)
        ContentUris.appendId(builder, now + 5 * 60 * 1000)

        var inMeeting = false
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                null
            )
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val title = cursor.getString(0).lowercase()
                    Log.d(TAG, "Active calendar event detected: $title")
                    if (title.contains("meeting") || title.contains("interview") || 
                        title.contains("emergency") || title.contains("standup") || 
                        title.contains("call")) {
                        inMeeting = true
                        break
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying calendar provider", e)
        } finally {
            cursor?.close()
        }
        return inMeeting
    }

    private fun sendPacket(cmd: Byte) {
        val socket = bluetoothSocket
        if (socket != null && socket.isConnected) {
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val out = socket.outputStream
                    synchronized(out) {
                        out.write(cmd.toInt())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send packet command: $cmd", e)
                }
            }
        }
    }

    private fun writePcmToWav(pcmData: ByteArray, wavFile: File) {
        val totalAudioLen = pcmData.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val sampleRate = 16000L
        val channels = 1
        val byteRate = sampleRate * channels * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        val out = FileOutputStream(wavFile)
        out.write(header)
        out.write(pcmData)
        out.close()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, getNotification(text))
    }

    private fun getNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Home Hub")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Home Hub Background Services",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun closeSocket() {
        try {
            bluetoothSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Bluetooth socket", e)
        }
        bluetoothSocket = null
        connectionState.value = ConnectionState.DISCONNECTED
    }



    override fun onDestroy() {
        super.onDestroy()
        isListeningLoopRunning = false
        mainHandler.removeCallbacksAndMessages(null)
        if (speechRecognizer != null) {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {}
        }
        serviceScope.cancel()
        closeSocket()
        tts.shutdown()
        Log.i(TAG, "Bluetooth Service destroyed.")
    }
}
