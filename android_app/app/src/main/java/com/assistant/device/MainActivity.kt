package com.assistant.device

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 200

    // Views
    private lateinit var providerSpinner: Spinner
    private lateinit var geminiConfigLayout: LinearLayout
    private lateinit var ollamaConfigLayout: LinearLayout
    private lateinit var keyInput: TextInputEditText
    private lateinit var ollamaEndpointInput: TextInputEditText
    private lateinit var ollamaModelInput: TextInputEditText
    private lateinit var ollamaApiKeyInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var permissionButton: MaterialButton
    private lateinit var notificationPermButton: MaterialButton
    private lateinit var btnSelectApps: MaterialButton
    private lateinit var btnClearNotifications: MaterialButton
    
    // Status Header Views
    private lateinit var statusCard: View
    private lateinit var statusLabel: TextView
    private lateinit var statusDetails: TextView
    private lateinit var statusPulseDot: View
    private lateinit var statusPulseBg: View
    
    // Stats Views
    private lateinit var totalCountText: TextView
    private lateinit var unreadCountText: TextView
    
    // Collapsible Settings Views
    private lateinit var settingsHeader: LinearLayout
    private lateinit var settingsContent: LinearLayout
    private lateinit var settingsToggleArrow: TextView

    // RecyclerView Views (Notifications)
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: NotificationAdapter

    // Voice Assistant Control Views
    private lateinit var voiceAssistantStatusText: TextView
    private lateinit var btnMic: FloatingActionButton
    private lateinit var micPulseRing: View
    private lateinit var tvTranscribedQuery: TextView
    private lateinit var tvAssistantResponse: TextView

    // Quick Command Pills
    private lateinit var cmdPlayMusic: MaterialButton
    private lateinit var cmdOpenWhatsapp: MaterialButton
    private lateinit var cmdOpenCalendar: MaterialButton
    private lateinit var cmdOpenMaps: MaterialButton

    // Media Controller Views
    private lateinit var btnPrev: MaterialButton
    private lateinit var btnPlayPause: MaterialButton
    private lateinit var btnNext: MaterialButton

    private lateinit var database: AssistantDatabase
    private var pulseAnimation: AlphaAnimation? = null
    private var micPulseAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AssistantDatabase.getDatabase(applicationContext)

        // Bind Status Header Views
        statusCard = findViewById(R.id.statusCard)
        statusLabel = findViewById(R.id.statusLabel)
        statusDetails = findViewById(R.id.statusDetails)
        statusPulseDot = findViewById(R.id.statusPulseDot)
        statusPulseBg = findViewById(R.id.statusPulseBg)

        // Bind Stats Views
        totalCountText = findViewById(R.id.totalCountText)
        unreadCountText = findViewById(R.id.unreadCountText)

        // Bind RecyclerView Views
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)

        // Bind Collapsible Settings Views
        settingsHeader = findViewById(R.id.settingsHeader)
        settingsContent = findViewById(R.id.settingsContent)
        settingsToggleArrow = findViewById(R.id.settingsToggleArrow)
        providerSpinner = findViewById(R.id.providerSpinner)
        geminiConfigLayout = findViewById(R.id.geminiConfigLayout)
        ollamaConfigLayout = findViewById(R.id.ollamaConfigLayout)
        keyInput = findViewById(R.id.keyInput)
        ollamaEndpointInput = findViewById(R.id.ollamaEndpointInput)
        ollamaModelInput = findViewById(R.id.ollamaModelInput)
        ollamaApiKeyInput = findViewById(R.id.ollamaApiKeyInput)
        saveButton = findViewById(R.id.saveButton)
        permissionButton = findViewById(R.id.permissionButton)
        notificationPermButton = findViewById(R.id.notificationPermButton)
        btnSelectApps = findViewById(R.id.btnSelectApps)
        btnClearNotifications = findViewById(R.id.btnClearNotifications)

        settingsHeader.setOnClickListener { toggleSettingsPanel() }

        // Setup AI Provider Spinner options
        val providerOptions = arrayOf("Gemini AI (Cloud)", "Ollama (Local)")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, providerOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        providerSpinner.adapter = spinnerAdapter

        // Load Settings
        val sharedPrefs = getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE)
        val savedProvider = sharedPrefs.getString("ai_provider", "gemini") ?: "gemini"
        val savedGeminiKey = sharedPrefs.getString("gemini_api_key", "") ?: ""
        
        var savedOllamaEndpoint = sharedPrefs.getString("ollama_endpoint", "https://ollama.com") ?: "https://ollama.com"
        if (savedOllamaEndpoint.contains("localhost") || savedOllamaEndpoint.contains("127.0.0.1") || savedOllamaEndpoint.contains("10.0.2.2") || savedOllamaEndpoint.contains("ollama.com/api")) {
            savedOllamaEndpoint = "https://ollama.com"
        }
        
        var savedOllamaModel = sharedPrefs.getString("ollama_model", "gpt-oss:120b-cloud") ?: "gpt-oss:120b-cloud"
        if (savedOllamaModel == "llama3" || savedOllamaModel == "gpt-oss:120b") {
            savedOllamaModel = "gpt-oss:120b-cloud"
        }
        
        var savedOllamaKey = sharedPrefs.getString("ollama_api_key", "206cf8d5978245ca97f529cb37d20545") ?: "206cf8d5978245ca97f529cb37d20545"
        if (savedOllamaKey.isEmpty()) {
            savedOllamaKey = "206cf8d5978245ca97f529cb37d20545"
        }

        // Set values in inputs
        if (savedProvider == "ollama") {
            providerSpinner.setSelection(1)
            geminiConfigLayout.visibility = View.GONE
            ollamaConfigLayout.visibility = View.VISIBLE
        } else {
            providerSpinner.setSelection(0)
            geminiConfigLayout.visibility = View.VISIBLE
            ollamaConfigLayout.visibility = View.GONE
        }
        keyInput.setText(savedGeminiKey)
        ollamaEndpointInput.setText(savedOllamaEndpoint)
        ollamaModelInput.setText(savedOllamaModel)
        ollamaApiKeyInput.setText(savedOllamaKey)

        // Switch panel visibilities on Spinner change
        providerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    geminiConfigLayout.visibility = View.VISIBLE
                    ollamaConfigLayout.visibility = View.GONE
                } else {
                    geminiConfigLayout.visibility = View.GONE
                    ollamaConfigLayout.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        saveButton.setOnClickListener {
            val selectedPosition = providerSpinner.selectedItemPosition
            val provider = if (selectedPosition == 0) "gemini" else "ollama"
            val geminiKey = keyInput.text.toString().trim()
            val ollamaEndpoint = ollamaEndpointInput.text.toString().trim()
            val ollamaModel = ollamaModelInput.text.toString().trim()
            val ollamaKey = ollamaApiKeyInput.text.toString().trim()

            if (provider == "gemini" && geminiKey.isEmpty()) {
                Toast.makeText(this, "Please enter Gemini API Key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (provider == "ollama" && ollamaEndpoint.isEmpty()) {
                Toast.makeText(this, "Please enter Ollama endpoint URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (provider == "ollama" && ollamaModel.isEmpty()) {
                Toast.makeText(this, "Please enter Ollama model name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPrefs.edit().apply {
                putString("ai_provider", provider)
                putString("gemini_api_key", geminiKey)
                putString("ollama_endpoint", ollamaEndpoint)
                putString("ollama_model", ollamaModel)
                putString("ollama_api_key", ollamaKey)
            }.apply()

            Toast.makeText(this, "Brain Settings Saved!", Toast.LENGTH_SHORT).show()

            // Notify background service to reload clients without dropping Bluetooth connection
            val reloadIntent = Intent(this, BluetoothService::class.java).apply {
                action = "com.assistant.device.action.RELOAD_CONFIG"
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(reloadIntent)
            } else {
                startService(reloadIntent)
            }
        }

        permissionButton.setOnClickListener {
            requestAppPermissions()
        }

        notificationPermButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }

        btnSelectApps.setOnClickListener {
            showAppSelectionDialog()
        }

        btnClearNotifications.setOnClickListener {
            lifecycleScope.launch {
                database.notificationDao().deleteAllNotifications()
                Toast.makeText(this@MainActivity, "All notifications cleared", Toast.LENGTH_SHORT).show()
            }
        }

        // Bind Voice Assistant Views
        voiceAssistantStatusText = findViewById(R.id.voiceAssistantStatusText)
        btnMic = findViewById(R.id.btnMic)
        micPulseRing = findViewById(R.id.micPulseRing)
        tvTranscribedQuery = findViewById(R.id.tvTranscribedQuery)
        tvAssistantResponse = findViewById(R.id.tvAssistantResponse)

        // Bind Quick Commands
        cmdPlayMusic = findViewById(R.id.cmdPlayMusic)
        cmdOpenWhatsapp = findViewById(R.id.cmdOpenWhatsapp)
        cmdOpenCalendar = findViewById(R.id.cmdOpenCalendar)
        cmdOpenMaps = findViewById(R.id.cmdOpenMaps)

        // Bind Media Buttons
        btnPrev = findViewById(R.id.btnPrev)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)

        // Setup RecyclerView
        adapter = NotificationAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Mic Button Click Listener - triggers listening inside the background service
        btnMic.setOnClickListener {
            checkPermissionsAndTriggerListening()
        }

        // Quick Commands Click Handlers (forward actions directly or simulate speech input)
        cmdPlayMusic.setOnClickListener {
            triggerBackgroundCommand("Play Music")
        }
        cmdOpenWhatsapp.setOnClickListener {
            triggerBackgroundCommand("Open WhatsApp")
        }
        cmdOpenCalendar.setOnClickListener {
            triggerBackgroundCommand("Open Calendar")
        }
        cmdOpenMaps.setOnClickListener {
            triggerBackgroundCommand("Open Maps")
        }

        // Media Controls Click Handlers
        btnPlayPause.setOnClickListener {
            executeMusicAction(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
        btnPrev.setOnClickListener {
            executeMusicAction(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
        btnNext.setOnClickListener {
            executeMusicAction(KeyEvent.KEYCODE_MEDIA_NEXT)
        }

        // Redundant listeners removed

        // Start background service
        startBluetoothService()

        // Setup Observers
        setupConnectionObserver()
        setupNotificationObservers()
        setupVoiceAssistantObservers()
    }

    override fun onResume() {
        super.onResume()
        updateNotificationAccessStatus()
    }

    private fun updateNotificationAccessStatus() {
        val enabled = isNotificationServiceEnabled()
        if (!enabled) {
            notificationPermButton.text = "⚠️ Grant Notification Access"
            notificationPermButton.setBackgroundColor(ContextCompat.getColor(this, R.color.error))
        } else {
            notificationPermButton.text = "✅ Notification Access Active"
            notificationPermButton.setBackgroundColor(ContextCompat.getColor(this, R.color.success))
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val cn = android.content.ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == pkgName) {
                    return true
                }
            }
        }
        return false
    }

    private fun toggleSettingsPanel() {
        if (settingsContent.visibility == View.VISIBLE) {
            settingsContent.visibility = View.GONE
            settingsToggleArrow.text = "▲"
        } else {
            settingsContent.visibility = View.VISIBLE
            settingsToggleArrow.text = "▼"
        }
    }

    private fun startPulseAnimation() {
        if (pulseAnimation == null) {
            pulseAnimation = AlphaAnimation(0.2f, 1.0f).apply {
                duration = 1000
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }
        }
        statusPulseBg.startAnimation(pulseAnimation)
    }

    private fun stopPulseAnimation() {
        statusPulseBg.clearAnimation()
    }

    private fun startMicPulseAnimation() {
        micPulseRing.visibility = View.VISIBLE
        if (micPulseAnimation == null) {
            micPulseAnimation = ScaleAnimation(
                1.0f, 1.3f, 1.0f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 800
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }
        }
        micPulseRing.startAnimation(micPulseAnimation)
    }

    private fun stopMicPulseAnimation() {
        micPulseRing.clearAnimation()
        micPulseRing.visibility = View.INVISIBLE
    }

    private fun setupConnectionObserver() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                BluetoothService.connectionState.collectLatest { state ->
                    when (state) {
                        ConnectionState.CONNECTED -> {
                            statusCard.setBackgroundResource(R.drawable.bg_glass_card_connected)
                            statusLabel.text = "Assistant Connected"
                            statusDetails.text = "Standby - Connected to HomeHub Hardware"
                            statusPulseDot.setBackgroundResource(R.drawable.bg_pulse_ring)
                            statusPulseBg.setBackgroundResource(R.drawable.bg_pulse_ring)
                            statusPulseDot.visibility = View.VISIBLE
                            statusPulseBg.visibility = View.VISIBLE
                            startPulseAnimation()
                        }
                        ConnectionState.CONNECTING -> {
                            statusCard.setBackgroundResource(R.drawable.bg_glass_card)
                            statusLabel.text = "Searching..."
                            statusDetails.text = "Attempting Bluetooth Hardware Connection..."
                            statusPulseDot.setBackgroundResource(R.drawable.bg_pulse_ring)
                            statusPulseBg.setBackgroundResource(R.drawable.bg_pulse_ring)
                            statusPulseDot.visibility = View.VISIBLE
                            statusPulseBg.visibility = View.VISIBLE
                            startPulseAnimation()
                        }
                        ConnectionState.DISCONNECTED -> {
                            statusCard.setBackgroundResource(R.drawable.bg_glass_card)
                            statusLabel.text = "Standalone Mode"
                            statusDetails.text = "Hardware offline - Voice assistant active on phone"
                            stopPulseAnimation()
                            statusPulseDot.visibility = View.INVISIBLE
                            statusPulseBg.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun setupNotificationObservers() {
        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    database.notificationDao().getRecentNotificationsFlow().collectLatest { list ->
                        adapter.submitList(list)
                        if (list.isEmpty()) {
                            emptyView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                }
                launch {
                    database.notificationDao().getTotalCountFlow(todayMidnight).collectLatest { count ->
                        totalCountText.text = count.toString()
                    }
                }
                launch {
                    database.notificationDao().getUnreadCountFlow(todayMidnight).collectLatest { count ->
                        unreadCountText.text = count.toString()
                    }
                }
            }
        }
    }

    // --- Active Voice Assistant Observers ---

    private fun setupVoiceAssistantObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    BluetoothService.lastTranscribedQuery.collectLatest { query ->
                        if (query.isNotEmpty()) {
                            tvTranscribedQuery.text = "You: \"$query\""
                        }
                    }
                }
                launch {
                    BluetoothService.lastAssistantResponse.collectLatest { response ->
                        if (response.isNotEmpty()) {
                            tvAssistantResponse.text = "Assistant: \"$response\""
                        }
                    }
                }
                launch {
                    BluetoothService.assistantSpeechState.collectLatest { state ->
                        voiceAssistantStatusText.text = state
                        if (state == "Listening...") {
                            startMicPulseAnimation()
                        } else {
                            stopMicPulseAnimation()
                        }
                    }
                }
                launch {
                    BluetoothService.isListeningActive.collectLatest { isActive ->
                        if (isActive && BluetoothService.assistantMode.value == AssistantState.ACTIVE) {
                            btnMic.setImageResource(android.R.drawable.ic_btn_speak_now)
                            btnMic.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.primary))
                        } else {
                            btnMic.setImageResource(android.R.drawable.ic_lock_silent_mode)
                            btnMic.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.slate_400))
                        }
                    }
                }
                launch {
                    BluetoothService.assistantMode.collectLatest { mode ->
                        if (mode == AssistantState.SLEEP) {
                            voiceAssistantStatusText.text = "HomeHub Asleep (Say 'HomeHub wake up')"
                            btnMic.setImageResource(android.R.drawable.ic_lock_silent_mode)
                            btnMic.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.slate_400))
                            stopMicPulseAnimation()
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissionsAndTriggerListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
        } else {
            toggleBackgroundListening()
        }
    }

    private fun toggleBackgroundListening() {
        val toggleIntent = Intent(this, BluetoothService::class.java).apply {
            action = BluetoothService.ACTION_TOGGLE_LISTENING
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(toggleIntent)
        } else {
            startService(toggleIntent)
        }
    }

    private fun triggerBackgroundListening() {
        val startIntent = Intent(this, BluetoothService::class.java).apply {
            action = BluetoothService.ACTION_START_LISTENING
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent)
        } else {
            startService(startIntent)
        }
    }

    private fun triggerBackgroundCommand(command: String) {
        tvTranscribedQuery.text = "You: (Tap) $command"
        // Send a custom broadcast or invoke listening to direct user's mock intent
        val startIntent = Intent(this, BluetoothService::class.java).apply {
            action = BluetoothService.ACTION_START_LISTENING
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent)
        } else {
            startService(startIntent)
        }
        Toast.makeText(this, "Active Listening started...", Toast.LENGTH_SHORT).show()
    }

    private fun executeMusicAction(keyCode: Int) {
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
                Log.d("MainActivity", "Music app launch skipped: ${e.message}")
            }
        }

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startBluetoothService() {
        try {
            val intent = Intent(this, BluetoothService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start foreground service on launch: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                // Trigger background service listening immediately since permission was just granted
                triggerBackgroundListening()
            } else {
                Toast.makeText(this, "Some permissions denied. Voice recognition might be blocked.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showAppSelectionDialog() {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        
        var appsList = resolveInfos.map {
            Pair(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.second }.sortedBy { it.first }

        // Robust fallback for device package managers
        if (appsList.isEmpty()) {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            appsList = installedApps.filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
                .map { Pair(it.loadLabel(pm).toString(), it.packageName) }
                .distinctBy { it.second }
                .sortedBy { it.first }
        }

        if (appsList.isEmpty()) {
            Toast.makeText(this, "No apps found on device.", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPrefs = getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE)
        val savedSelected = sharedPrefs.getStringSet("selected_notification_apps", null) ?: emptySet()
        val selectedPackages = HashSet<String>(if (savedSelected.isEmpty()) appsList.map { it.second } else savedSelected)

        // Build Custom Dialog View with Search Bar and Quick Deselect/Select All Controls
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 16)
        }

        val searchEditText = EditText(this).apply {
            hint = "🔍 Search apps (e.g. WhatsApp, Gmail)..."
            setSingleLine(true)
            textSize = 14f
            setPadding(32, 24, 32, 24)
            background = ContextCompat.getDrawable(this@MainActivity, android.R.drawable.editbox_background)
        }
        container.addView(searchEditText)

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }
        val btnDeselectAll = android.widget.Button(this, null, android.R.attr.borderlessButtonStyle).apply {
            text = "✖ Deselect All"
            textSize = 13f
            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val btnSelectAll = android.widget.Button(this, null, android.R.attr.borderlessButtonStyle).apply {
            text = "✓ Select All"
            textSize = 13f
            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_dark))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        actionRow.addView(btnDeselectAll)
        actionRow.addView(btnSelectAll)
        container.addView(actionRow)

        val listView = android.widget.ListView(this).apply {
            choiceMode = android.widget.ListView.CHOICE_MODE_MULTIPLE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                800
            )
        }
        container.addView(listView)

        var filteredApps = appsList.toList()
        fun createAdapter() = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            filteredApps.map { it.first }
        )

        var listAdapter = createAdapter()
        listView.adapter = listAdapter

        fun syncListViewChecks() {
            for (i in filteredApps.indices) {
                val pkg = filteredApps[i].second
                listView.setItemChecked(i, selectedPackages.contains(pkg))
            }
        }
        syncListViewChecks()

        listView.setOnItemClickListener { _, _, position, _ ->
            val pkg = filteredApps[position].second
            if (listView.isItemChecked(position)) {
                selectedPackages.add(pkg)
            } else {
                selectedPackages.remove(pkg)
            }
        }

        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                filteredApps = if (query.isEmpty()) {
                    appsList
                } else {
                    appsList.filter { it.first.lowercase().contains(query) || it.second.lowercase().contains(query) }
                }
                listAdapter = createAdapter()
                listView.adapter = listAdapter
                syncListViewChecks()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        btnDeselectAll.setOnClickListener {
            selectedPackages.clear()
            syncListViewChecks()
            Toast.makeText(this, "Deselected all apps", Toast.LENGTH_SHORT).show()
        }

        btnSelectAll.setOnClickListener {
            selectedPackages.addAll(appsList.map { it.second })
            syncListViewChecks()
            Toast.makeText(this, "Selected all apps", Toast.LENGTH_SHORT).show()
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Filter Notification Apps")
            .setView(container)
            .setPositiveButton("Save Filter") { dialog, _ ->
                sharedPrefs.edit().putStringSet("selected_notification_apps", selectedPackages).apply()
                Toast.makeText(this, "Notification Filter Saved (${selectedPackages.size} apps monitored)", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPulseAnimation()
        stopMicPulseAnimation()
    }
}
