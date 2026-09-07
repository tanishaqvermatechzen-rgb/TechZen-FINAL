package com.assistant.device

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OllamaClient(
    private val context: Context,
    private val endpointUrl: String,
    private val modelName: String,
    private val apiKey: String? = null
) {
    private val TAG = "OllamaClient"
    private val database = AssistantDatabase.getDatabase(context)
    private val sanitizedEndpoint = endpointUrl.trim().removeSuffix("/").removeSuffix("/api").removeSuffix("/")
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val chatHistory = mutableListOf<JSONObject>()

    fun resetChatSession() {
        chatHistory.clear()
    }

    suspend fun getResponse(userQuery: String): String = withContext(Dispatchers.IO) {
        try {
            val sinceTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            val gatewayContext = AiGateway(context).gatherGatewayContext()

            val systemInstruction = """
                You are the brain of a Physical Voice Device (Home Hub Pod).
                You act as a personal hands-free interface for the user's Android phone.
                You have access to real-time data from the Phone Gateway (notifications, calendar, media).
                
                CLASSIFY AND PROCESS EVERY USER QUESTION IN THIS STRICT ORDER ONLY:

                ORDER 1: PERSONAL & PHONE-RELATED QUERY CHECK:
                   - First, check if the question is personal to the user or related to their phone data (WhatsApp messages, Gmail emails, 36h Calendar schedule, active notifications, phone settings, installed apps).
                   - If YES: Use the provided "Context" block or use the action tags below to answer directly.
                   - ALWAYS mention exact details (who messaged, app name, message body, time).

                ORDER 2: GENERAL KNOWLEDGE & IQ GOOGLE QUESTION CHECK:
                   - If NOT personal/phone-related, check if the question is an IQ, factual, news, weather, or general knowledge question that can be found on Google.
                   - If YES: Use your general intelligence AND the [WEB_SEARCH] tag to provide a direct, accurate factual answer.

                ORDER 3: OPEN PROBLEM-SOLVING & REASONING CHECK:
                   - If NOT personal/phone-related AND NOT a simple Google/IQ question, determine what the complex problem or question is, explain what the core question is, and provide a clear step-by-step solution to solve it.

                ACTION SAFETY GUARD (SENSITIVE ACTION CONFIRMATION):
                - Before dispatching any external message (such as sending a WhatsApp message) or scheduling a new calendar event, DO NOT execute the tool function immediately unless the user explicitly said "yes", "confirm", or "send it" to a previous confirmation prompt.
                - When the user asks to send a message or schedule an event for the first time, ask for explicit voice confirmation in 1 short sentence instead of calling the tool:
                  * Example for WhatsApp: "I am ready to send: '[Message Body]' to [Contact Name]. Should I send this message?"
                  * Example for Calendar: "I am ready to schedule '[Event Title]' at [Start Time]. Should I create this event?"
                - ONLY execute the tool function ('send_whatsapp_message', 'create_calendar_event') after the user explicitly confirms with "yes", "send it", "confirm", "go ahead", or "do it".
                - If the user says "no", "cancel", or "stop", abort the action immediately and reply: "Action cancelled."

                If you need to perform an action or search, use these tags:
                - [SEARCH: <query>] - Search historical notifications (24h memory).
                - [WEB_SEARCH: <query>] - Search the internet for facts.
                - [WHATSAPP: <contact> | <message>] - Send a WhatsApp message.
                - [CALENDAR: <title> | <iso_time>] - Create a calendar event.
                - [OPEN_APP: <app_name>] - Open an app on the phone.

                Keep responses short, conversational, and direct. 
                Always start with the tag if an action is needed.
            """.trimIndent()

            val initialUserMsg = "Context:\n$gatewayContext\n\nUser Query: $userQuery"

            // Build request messages including history
            val messagesArray = JSONArray()
            
            // Add system instruction first
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", systemInstruction)
            })

            // Add history
            for (msg in chatHistory) {
                messagesArray.put(msg)
            }

            // Add current user message
            val currentMsg = JSONObject().apply {
                put("role", "user")
                put("content", initialUserMsg)
            }
            messagesArray.put(currentMsg)

            var assistantResponse = queryOllama(messagesArray)

            // Loop to handle potential action tags
            var retryCount = 0
            while (assistantResponse.startsWith("[") && assistantResponse.contains("]") && retryCount < 2) {
                val tagEnd = assistantResponse.indexOf("]")
                val tagContent = assistantResponse.substring(1, tagEnd)
                val parts = tagContent.split(":", limit = 2)
                if (parts.size < 2) break
                
                val cmd = parts[0].trim()
                val args = parts[1].trim()
                
                Log.d(TAG, "Ollama requested action: $cmd with args: $args")
                
                val toolResult = when (cmd) {
                    "SEARCH" -> executeHistoricalSearch(args, sinceTime)
                    "WEB_SEARCH" -> executeWebSearch(args)
                    "WHATSAPP" -> {
                        val whatsappParts = args.split("|", limit = 2)
                        if (whatsappParts.size == 2) executeWhatsAppMessage(whatsappParts[0].trim(), whatsappParts[1].trim())
                        else "Invalid WhatsApp tag format. Use [WHATSAPP: name | message]"
                    }
                    "CALENDAR" -> {
                        val calParts = args.split("|", limit = 2)
                        if (calParts.size == 2) executeCreateCalendarEvent(calParts[0].trim(), calParts[1].trim())
                        else "Invalid Calendar tag format. Use [CALENDAR: title | time]"
                    }
                    "OPEN_APP" -> executeOpenApp(args)
                    else -> "Unknown command: $cmd"
                }

                // Add assistant call and tool response to history temporarily for the follow-up
                val tempAssistantMsg = JSONObject().apply {
                    put("role", "assistant")
                    put("content", assistantResponse)
                }
                val tempUserResult = JSONObject().apply {
                    put("role", "user")
                    put("content", "Action result: $toolResult\nNow formulate your final spoken response.")
                }
                
                messagesArray.put(tempAssistantMsg)
                messagesArray.put(tempUserResult)

                assistantResponse = queryOllama(messagesArray)
                retryCount++
            }

            // Update persistent history
            chatHistory.add(currentMsg)
            chatHistory.add(JSONObject().apply {
                put("role", "assistant")
                put("content", assistantResponse)
            })

            // Keep history limited to last 20 messages to avoid context bloat
            if (chatHistory.size > 20) {
                repeat(2) { chatHistory.removeAt(0) }
            }

            // Mark the processed notifications as read
            database.notificationDao().markLast24HoursAsRead(sinceTime)

            return@withContext assistantResponse

        } catch (e: Exception) {
            Log.e(TAG, "Error generating response from Ollama", e)
            return@withContext "Sorry, I had trouble connecting to the local assistant brain. ${e.message}"
        }
    }


    private fun queryOllama(messages: JSONArray): String {
        val requestUrl = "$sanitizedEndpoint/api/chat"
        
        val requestBodyJson = JSONObject().apply {
            put("model", modelName)
            put("messages", messages)
            put("stream", false)
        }

        val requestBuilder = Request.Builder()
            .url(requestUrl)
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))

        if (!apiKey.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "Ollama request failed: ${response.code} $errorBody")
                throw Exception("Ollama HTTP Error: ${response.code}")
            }

            val bodyString = response.body?.string() ?: throw Exception("Empty response body from Ollama")
            val jsonResponse = JSONObject(bodyString)
            val messageJson = jsonResponse.getJSONObject("message")
            return messageJson.getString("content").trim()
        }
    }

    suspend fun getWebResponse(userQuery: String): String = withContext(Dispatchers.IO) {
        try {
            return@withContext queryOllamaGenerate(userQuery)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating web response from Ollama", e)
            return@withContext "Sorry, I had trouble getting an answer from the web. ${e.message}"
        }
    }

    private fun queryOllamaGenerate(prompt: String): String {
        val requestUrl = "$sanitizedEndpoint/api/generate"
        
        val requestBodyJson = JSONObject().apply {
            put("model", modelName)
            put("prompt", prompt)
            put("stream", false)
        }

        val requestBuilder = Request.Builder()
            .url(requestUrl)
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))

        if (!apiKey.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "Ollama request failed: ${response.code} $errorBody")
                throw Exception("Ollama HTTP Error: ${response.code}")
            }

            val bodyString = response.body?.string() ?: throw Exception("Empty response body from Ollama")
            val jsonResponse = JSONObject(bodyString)
            return jsonResponse.getString("response").trim()
        }
    }

    private suspend fun executeHistoricalSearch(query: String, sinceTime: Long): String {
        val olderNotifs = database.notificationDao().getOlderNotifications(sinceTime)
        
        val filtered = olderNotifs.filter {
            it.text.contains(query, ignoreCase = true) || 
            (it.sender?.contains(query, ignoreCase = true) ?: false)
        }

        if (filtered.isEmpty()) {
            return "No matching older notifications found."
        }

        val resultBuilder = StringBuilder()
        for (notif in filtered.take(10)) {
            val dateStr = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a", notif.timestamp)
            resultBuilder.append("- [$dateStr] Sender: ${notif.sender ?: "Unknown"}, Message: ${notif.text}\n")
        }
        return resultBuilder.toString()
    }

    private fun executeWebSearch(query: String): String {
        Log.i(TAG, "Executing web search for: $query")
        return "Search results for '$query': [Simulated Web Search Result] Information found on the web suggests that $query is a common topic of interest with several recent updates."
    }

    private fun executeWhatsAppMessage(contact: String, message: String): String {
        Log.i(TAG, "Executing WhatsApp message to $contact: $message")
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

    private fun executeCreateCalendarEvent(title: String, startTime: String): String {
        Log.i(TAG, "Creating calendar event: $title at $startTime")
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                data = android.provider.CalendarContract.Events.CONTENT_URI
                putExtra(android.provider.CalendarContract.Events.TITLE, title)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return "Opened Calendar to create event: $title."
        } catch (e: Exception) {
            return "Failed to open Calendar: ${e.message}"
        }
    }

    private fun executeOpenApp(appName: String): String {
        Log.i(TAG, "Opening app: $appName")
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        
        val targetApp = packages.find { 
            val label = pm.getApplicationLabel(it).toString()
            label.contains(appName, ignoreCase = true)
        }

        if (targetApp != null) {
            val intent = pm.getLaunchIntentForPackage(targetApp.packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return "Opening $appName."
            }
        }
        return "Could not find an app named $appName installed on this device."
    }
}
