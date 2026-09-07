package com.assistant.device

import android.content.Context
import android.util.Log
import com.assistant.device.integrations.AppLauncherIntegration
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiClient(
    private val context: Context,
    rawApiKey: String,
    rawModelName: String = "gemini-flash-latest"
) {
    private val TAG = "GeminiClient"
    private val database = AssistantDatabase.getDatabase(context)
    val apiKey = if (rawApiKey.isBlank()) "AQ.Ab8RN6LX_reZkhcO8CQv02-6fOEU8lMEMkP5VTOaqKkwVJWW6A" else rawApiKey
    val modelName = if (rawModelName.isBlank() || rawModelName == "gemini-1.5-flash" || rawModelName == "gemini-2.0-flash") "gemini-flash-latest" else rawModelName

    // Define tools for Gemini
    private val tools = Tool(
        functionDeclarations = listOf(
            FunctionDeclaration(
                name = "search_historical_notifications",
                description = "Searches for WhatsApp messages, emails, and other phone notifications older than 24 hours.",
                parameters = listOf(
                    Schema.str(
                        name = "query",
                        description = "Key search terms or name of the sender to search for in historical database."
                    )
                ),
                requiredParameters = listOf("query")
            ),
            FunctionDeclaration(
                name = "search_web",
                description = "Performs a real-time web search for facts, news, or general knowledge questions that cannot be answered by local context.",
                parameters = listOf(
                    Schema.str(
                        name = "query",
                        description = "The search query for the web."
                    )
                ),
                requiredParameters = listOf("query")
            ),
            FunctionDeclaration(
                name = "send_whatsapp_message",
                description = "Sends a WhatsApp message to a specific contact or group.",
                parameters = listOf(
                    Schema.str(name = "contact", description = "The name of the contact or phone number."),
                    Schema.str(name = "message", description = "The content of the message to send.")
                ),
                requiredParameters = listOf("contact", "message")
            ),
            FunctionDeclaration(
                name = "create_calendar_event",
                description = "Schedules a new event in the user's calendar.",
                parameters = listOf(
                    Schema.str(name = "title", description = "Title of the event."),
                    Schema.str(name = "start_time", description = "Start time in ISO 8601 format (e.g., 2024-08-25T14:00:00)."),
                    Schema.int(name = "duration_minutes", description = "Duration of the event in minutes.")
                ),
                requiredParameters = listOf("title", "start_time")
            ),
            FunctionDeclaration(
                name = "open_app",
                description = "Opens any installed application on the user's phone.",
                parameters = listOf(
                    Schema.str(name = "app_name", description = "The common name of the app (e.g., Spotify, Instagram, Gmail).")
                ),
                requiredParameters = listOf("app_name")
            ),
            FunctionDeclaration(
                name = "list_installed_apps",
                description = "Lists all applications installed on the user's phone to know what is available.",
                parameters = listOf(),
                requiredParameters = emptyList()
            ),
            FunctionDeclaration(
                name = "control_smart_device",
                description = "Controls a smart home device (light, thermostat, lock, plug).",
                parameters = listOf(
                    Schema.str(name = "device_id", description = "The unique ID of the device."),
                    Schema.str(name = "action", description = "The action to perform: 'turn_on', 'turn_off', 'set_value'."),
                    Schema.int(name = "value", description = "Optional integer value for brightness or temperature.")
                ),
                requiredParameters = listOf("device_id", "action")
            ),
            FunctionDeclaration(
                name = "control_media",
                description = "Controls media playback on the phone.",
                parameters = listOf(
                    Schema.str(name = "action", description = "The action: 'play', 'pause', 'next', 'previous', 'stop'.")
                ),
                requiredParameters = listOf("action")
            ),
            FunctionDeclaration(
                name = "clear_notifications",
                description = "Clears all recent notifications from the assistant's view.",
                parameters = listOf(),
                requiredParameters = emptyList()
            )
        )
    )

    private val model = GenerativeModel(
        modelName = modelName,
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.3f
        },
        tools = listOf(tools),
        systemInstruction = content {
            text(
                "You are the brain of the user's Physical AI Personal Assistant Device (Home Hub Pod & Phone Gateway).\n" +
                "You are proactive, helpful, and concise. Your goal is to simplify the user's life by managing their digital and physical environment.\n\n" +
                "CORE CAPABILITIES:\n" +
                "1. PERSONAL DATA: You can access notifications, calendar, and media status via the 'Context' block.\n" +
                "2. SMART HOME: You can see the status of smart devices and control them using 'control_smart_device'.\n" +
                "3. MEDIA: You can control music/media playback using 'control_media'.\n" +
                "4. COMMUNICATION: You can send WhatsApp messages.\n" +
                "5. ORGANIZATION: You can schedule calendar events and clear notifications.\n" +
                "6. WEB SEARCH: Use 'search_web' for real-time information.\n\n" +
                "PROCESSING ORDER:\n" +
                "ORDER 1: PERSONAL & DEVICE QUERIES (Check Context & use appropriate Tools).\n" +
                "ORDER 2: GENERAL KNOWLEDGE (Use intelligence & 'search_web').\n" +
                "ORDER 3: PROBLEM SOLVING (Explain and provide step-by-step solutions).\n\n" +
                "GUIDELINES:\n" +
                "- When asked about notifications, offer to summarize them if there are many.\n" +
                "- If a user asks to 'clear everything' or 'delete updates', use 'clear_notifications'.\n" +
                "- ALWAYS mention exact details (sender name, event time) when discussing personal data.\n" +
                "- ACTION SAFETY: Ask for explicit confirmation (e.g., \"Should I send this?\") before sending WhatsApp messages or creating calendar events.\n\n" +
                "RESPONSE STYLE:\n" +
                "- Speak directly and conversationally. Output is read out loud."
            )
        }

    )

    private var activeChat: Chat? = null

    fun resetChatSession() {
        activeChat = null
    }

    private fun getOrCreateChat(): Chat {
        if (activeChat == null) {
            activeChat = model.startChat()
        }
        return activeChat!!
    }

    private val appLauncherIntegration = AppLauncherIntegration(context)

    private fun isPersonalDataQuery(query: String): Boolean {
        val q = query.lowercase(java.util.Locale.getDefault()).trim()
        val personalKeywords = listOf(
            "notification", "message", "whatsapp", "gmail", "email", "mail",
            "calendar", "schedule", "event", "meeting", "reminder", "appointment",
            "unread", "who text", "who messaged", "who email", "who call",
            "updates", "my status", "what's new", "any message", "read message",
            "open whatsapp", "open spotify", "open calendar", "play music", "pause music",
            "next song", "previous song", "media", "playing", "sms", "text message"
        )
        return personalKeywords.any { q.contains(it) }
    }

    suspend fun getResponseStream(
        userQuery: String,
        onSentenceChunk: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val fullTextBuilder = StringBuilder()
        val sentenceBuffer = StringBuilder()
        try {
            val sinceTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            val promptText = if (isPersonalDataQuery(userQuery)) {
                val gatewayContext = AiGateway(context).gatherGatewayContext()
                "Context:\n$gatewayContext\n\nUser Query: $userQuery"
            } else {
                val currentDateStr = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a, EEEE", System.currentTimeMillis()).toString()
                "Current Time: $currentDateStr\n(Note: General knowledge query - no personal data attached)\n\nUser Query: $userQuery"
            }

            val promptContent = content {
                text(promptText)
            }

            val chat = getOrCreateChat()
            val responseStream = chat.sendMessageStream(promptContent)

            responseStream.collect { chunk ->
                val chunkText = chunk.text ?: ""
                fullTextBuilder.append(chunkText)
                sentenceBuffer.append(chunkText)

                var boundaryIndex = findSentenceBoundary(sentenceBuffer.toString())
                while (boundaryIndex != -1) {
                    val sentence = sentenceBuffer.substring(0, boundaryIndex + 1).trim()
                    if (sentence.isNotEmpty()) {
                        onSentenceChunk(sentence)
                    }
                    sentenceBuffer.delete(0, boundaryIndex + 1)
                    boundaryIndex = findSentenceBoundary(sentenceBuffer.toString())
                }
            }

            val remaining = sentenceBuffer.toString().trim()
            if (remaining.isNotEmpty()) {
                onSentenceChunk(remaining)
            }

            database.notificationDao().markLast24HoursAsRead(sinceTime)
            return@withContext fullTextBuilder.toString().ifBlank { "I heard you, but I couldn't formulate a response." }
        } catch (e: Exception) {
            Log.e(TAG, "Streaming response error, falling back to standard getResponse", e)
            val fallbackResponse = getResponse(userQuery)
            onSentenceChunk(fallbackResponse)
            return@withContext fallbackResponse
        }
    }

    private fun findSentenceBoundary(text: String): Int {
        for (i in text.indices) {
            val c = text[i]
            if (c == '.' || c == '?' || c == '!' || c == '\n') {
                return i
            }
        }
        return -1
    }

    suspend fun getResponse(userQuery: String): String = withContext(Dispatchers.IO) {
        try {
            val sinceTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            val promptText = if (isPersonalDataQuery(userQuery)) {
                val gatewayContext = AiGateway(context).gatherGatewayContext()
                "Context:\n$gatewayContext\n\nUser Query: $userQuery"
            } else {
                val currentDateStr = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a, EEEE", System.currentTimeMillis()).toString()
                "Current Time: $currentDateStr\n(Note: General knowledge query - no personal data attached)\n\nUser Query: $userQuery"
            }

            // Build request content
            val promptContent = content {
                text(promptText)
            }

            // Call model using Chat to manage conversation history and tool responses correctly
            val chat = getOrCreateChat()
            var response = chat.sendMessage(promptContent)

            // Loop to handle potential multiple tool calls (sequential)
            var retryCount = 0
            while (response.functionCalls.isNotEmpty() && retryCount < 3) {
                val functionCall = response.functionCalls.first()
                Log.d(TAG, "Gemini requested tool call: ${functionCall.name}")

                val toolResult = when (functionCall.name) {
                    "search_historical_notifications" -> {
                        val query = functionCall.args["query"] as? String ?: ""
                        executeHistoricalSearch(query, sinceTime)
                    }
                    "search_web" -> {
                        val query = functionCall.args["query"] as? String ?: ""
                        executeWebSearch(query)
                    }
                    "send_whatsapp_message" -> {
                        val contact = functionCall.args["contact"] as? String ?: ""
                        val message = functionCall.args["message"] as? String ?: ""
                        executeWhatsAppMessage(contact, message)
                    }
                    "create_calendar_event" -> {
                        val title = functionCall.args["title"] as? String ?: ""
                        val startTime = functionCall.args["start_time"] as? String ?: ""
                        val duration = try {
                            functionCall.args["duration_minutes"]?.toString()?.toInt() ?: 30
                        } catch (e: Exception) { 30 }
                        executeCreateCalendarEvent(title, startTime, duration)
                    }
                    "open_app" -> {
                        val appName = functionCall.args["app_name"] as? String ?: ""
                        executeOpenApp(appName)
                    }
                    "list_installed_apps" -> executeListApps()
                    "control_smart_device" -> {
                        val deviceId = functionCall.args["device_id"] as? String ?: ""
                        val action = functionCall.args["action"] as? String ?: ""
                        val value = try {
                            functionCall.args["value"]?.toString()?.toInt()
                        } catch (e: Exception) { null }
                        executeSmartDeviceControl(deviceId, action, value)
                    }
                    "control_media" -> {
                        val action = functionCall.args["action"] as? String ?: ""
                        executeMediaControl(action)
                    }
                    "clear_notifications" -> executeClearNotifications()
                    else -> "Unknown tool called."
                }

                val functionResponse = content("function") {
                    part(FunctionResponsePart(
                        name = functionCall.name,
                        response = JSONObject(mapOf("results" to toolResult))
                    ))
                }
                response = chat.sendMessage(functionResponse)
                retryCount++
            }

            // Mark the processed last-24-hour notifications as read so they won't trigger buzzing again
            database.notificationDao().markLast24HoursAsRead(sinceTime)

            return@withContext response.text ?: "I heard you, but I couldn't formulate a response."
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response from Gemini", e)
            if (e.message?.contains("404") == true || e.message?.contains("not found", ignoreCase = true) == true) {
                Log.w(TAG, "Gemini model failed, retrying with gemini-flash-latest...")
                try {
                    val gatewayContext = AiGateway(context).gatherGatewayContext()
                    val fallbackModel = GenerativeModel(
                        modelName = "gemini-flash-latest",
                        apiKey = apiKey,
                        generationConfig = generationConfig { temperature = 0.3f }
                    )
                    val promptContent = content { text("Context:\n$gatewayContext\n\nUser Query: $userQuery") }
                    val fbResponse = fallbackModel.generateContent(promptContent)
                    return@withContext fbResponse.text ?: "I heard you, but I couldn't formulate a response."
                } catch (fbEx: Exception) {
                    Log.e(TAG, "Fallback model gemini-flash-latest also failed: ${fbEx.message}", fbEx)
                    return@withContext "Google Gemini Error: ${fbEx.message ?: e.message}"
                }
            }
            return@withContext "Sorry, I encountered an error connecting to my brain. ${e.message}"
        }
    }

    suspend fun getResponse(wavBytes: ByteArray): String = withContext(Dispatchers.IO) {
        try {
            val sinceTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            val gatewayContext = AiGateway(context).gatherGatewayContext()

            // Build request content with audio blob
            val promptContent = content {
                blob("audio/wav", wavBytes)
                text(
                    "You are the brain of the user's custom virtual assistant hardware device. " +
                    "Listen to the user's spoken audio query in the audio blob, transcribe it, and answer it. " +
                    "You are conversational, direct, and concise (since responses are read out loud on a speaker). " +
                    "Here is the context of the user's phone's AI gateway:\n$gatewayContext\n\n" +
                    "If the user's spoken query requires searching for information older than what is provided in the context, " +
                    "you MUST call the 'search_historical_notifications' tool with a search query. " +
                    "Otherwise, answer their query directly based on the audio."
                )
            }

            // Call model using Chat to manage conversation history and tool responses correctly
            val chat = model.startChat()
            var response = chat.sendMessage(promptContent)

            // Loop to handle potential multiple tool calls (sequential)
            var retryCount = 0
            while (response.functionCalls.isNotEmpty() && retryCount < 3) {
                val functionCall = response.functionCalls.first()
                Log.d(TAG, "Gemini requested tool call: ${functionCall.name}")

                val toolResult = when (functionCall.name) {
                    "search_historical_notifications" -> {
                        val query = functionCall.args["query"] as? String ?: ""
                        executeHistoricalSearch(query, sinceTime)
                    }
                    "search_web" -> {
                        val query = functionCall.args["query"] as? String ?: ""
                        executeWebSearch(query)
                    }
                    "send_whatsapp_message" -> {
                        val contact = functionCall.args["contact"] as? String ?: ""
                        val message = functionCall.args["message"] as? String ?: ""
                        executeWhatsAppMessage(contact, message)
                    }
                    "create_calendar_event" -> {
                        val title = functionCall.args["title"] as? String ?: ""
                        val startTime = functionCall.args["start_time"] as? String ?: ""
                        val duration = try {
                            functionCall.args["duration_minutes"]?.toString()?.toInt() ?: 30
                        } catch (e: Exception) { 30 }
                        executeCreateCalendarEvent(title, startTime, duration)
                    }
                    "open_app" -> {
                        val appName = functionCall.args["app_name"] as? String ?: ""
                        executeOpenApp(appName)
                    }
                    "list_installed_apps" -> executeListApps()
                    "control_smart_device" -> {
                        val deviceId = functionCall.args["device_id"] as? String ?: ""
                        val action = functionCall.args["action"] as? String ?: ""
                        val value = try {
                            functionCall.args["value"]?.toString()?.toInt()
                        } catch (e: Exception) { null }
                        executeSmartDeviceControl(deviceId, action, value)
                    }
                    "control_media" -> {
                        val action = functionCall.args["action"] as? String ?: ""
                        executeMediaControl(action)
                    }
                    "clear_notifications" -> executeClearNotifications()
                    else -> "Unknown tool called."
                }

                val functionResponse = content("function") {
                    part(FunctionResponsePart(
                        name = functionCall.name,
                        response = JSONObject(mapOf("results" to toolResult))
                    ))
                }
                response = chat.sendMessage(functionResponse)
                retryCount++
            }

            // Mark the processed last-24-hour notifications as read
            database.notificationDao().markLast24HoursAsRead(sinceTime)

            return@withContext response.text ?: "I heard you, but I couldn't formulate a response."
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response for voice query from Gemini", e)
            return@withContext "Sorry, I encountered an error connecting to my brain. ${e.message}"
        }
    }

    suspend fun transcribeAudio(wavBytes: ByteArray): String = withContext(Dispatchers.IO) {
        try {
            val transcribeModelName = if (modelName == "gemini-1.5-flash") "gemini-2.0-flash" else modelName
            val transcribeModel = GenerativeModel(
                modelName = transcribeModelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.0f
                }
            )
            val promptContent = content {
                blob("audio/wav", wavBytes)
                text("Transcribe the spoken words in this audio exactly. Output only the plain transcribed text, nothing else. If there is no speech, output empty string.")
            }
            val response = transcribeModel.generateContent(promptContent)
            return@withContext response.text ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Audio transcription failed", e)
            return@withContext ""
        }
    }

    private suspend fun executeHistoricalSearch(query: String, sinceTime: Long): String {
        // Query SQLite for older notifications matching keywords or sender
        val olderNotifs = database.notificationDao().getOlderNotifications(sinceTime)
        
        // Filter results locally by keyword or sender to simulate full-text search
        val filtered = olderNotifs.filter {
            it.text.contains(query, ignoreCase = true) || 
            (it.sender?.contains(query, ignoreCase = true) ?: false)
        }

        if (filtered.isEmpty()) {
            return "No matching older notifications found in the database."
        }

        val resultBuilder = StringBuilder()
        for (notif in filtered.take(10)) { // Limit to top 10 matches to save tokens
            val dateStr = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a", notif.timestamp)
            resultBuilder.append("- [$dateStr] Sender: ${notif.sender ?: "Unknown"}, Message: ${notif.text}\n")
        }
        return resultBuilder.toString()
    }

    private fun executeWebSearch(query: String): String {
        Log.i(TAG, "Executing live web search over Wi-Fi for: $query")
        return try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = java.net.URL("https://html.duckduckgo.com/html/?q=$encodedQuery")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            conn.connectTimeout = 6000
            conn.readTimeout = 6000

            if (conn.responseCode == 200) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                val snippets = mutableListOf<String>()
                val matcher = java.util.regex.Pattern.compile("<a class=\"result__snippet[^\"]*\"[^>]*>(.*?)</a>", java.util.regex.Pattern.DOTALL).matcher(html)
                while (matcher.find() && snippets.size < 4) {
                    val rawSnippet = matcher.group(1)?.replace(Regex("<[^>]*>"), "")?.trim() ?: ""
                    if (rawSnippet.isNotEmpty()) {
                        snippets.add(rawSnippet)
                    }
                }
                if (snippets.isNotEmpty()) {
                    "Live Google/Web Search Results over Wi-Fi for '$query':\n\n" + snippets.joinToString("\n\n")
                } else {
                    "Live web search completed for '$query'. Web search active."
                }
            } else {
                "Live web search request returned HTTP code ${conn.responseCode}."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Live web search request failed", e)
            "Live web search request over Wi-Fi failed: ${e.message}"
        }
    }

    private fun executeWhatsAppMessage(contact: String, message: String): String {
        Log.i(TAG, "Executing WhatsApp message to $contact: $message")
        // Use an Intent to open WhatsApp with the pre-filled message
        // Note: Real "sending" without user interaction usually requires Accessibility Service or Business API
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://api.whatsapp.com/send?text=${android.net.Uri.encode(message)}")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            return "Opened WhatsApp to send message to $contact."
        } catch (e: Exception) {
            return "Failed to open WhatsApp: ${e.message}"
        }
    }

    private fun executeCreateCalendarEvent(title: String, startTime: String, duration: Int): String {
        Log.i(TAG, "Creating calendar event: $title at $startTime")
        // Parse ISO time and use CalendarContract
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                data = android.provider.CalendarContract.Events.CONTENT_URI
                putExtra(android.provider.CalendarContract.Events.TITLE, title)
                // Add more details if possible
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return "Opened Calendar to create event: $title."
        } catch (e: Exception) {
            return "Failed to open Calendar: ${e.message}"
        }
    }

    private fun executeOpenApp(appName: String): String {
        return appLauncherIntegration.openAppByName(appName)
    }

    private fun executeListApps(): String {
        return appLauncherIntegration.listInstalledApps()
    }

    private suspend fun executeSmartDeviceControl(deviceId: String, action: String, value: Int?): String {
        val device = database.smartDeviceDao().getDeviceById(deviceId)
            ?: return "Device with ID $deviceId not found."

        val updatedDevice = when (action) {
            "turn_on" -> device.copy(isOn = true)
            "turn_off" -> device.copy(isOn = false)
            "set_value" -> if (value != null) device.copy(value = value) else device
            else -> return "Invalid action: $action"
        }

        database.smartDeviceDao().insertOrUpdate(updatedDevice)
        return "Device ${device.name} updated: $action ${value ?: ""}."
    }

    private fun executeMediaControl(action: String): String {
        val mediaIntegration = com.assistant.device.integrations.MediaIntegration(context)
        val success = mediaIntegration.executeMediaControl(action)
        return if (success) "Media control '$action' executed." else "Failed to execute media control '$action'."
    }

    private suspend fun executeClearNotifications(): String {
        database.notificationDao().deleteAllNotifications()
        return "All recent notifications have been cleared."
    }
}
