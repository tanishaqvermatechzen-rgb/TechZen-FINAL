package com.assistant.device.integrations

import android.content.Context
import com.assistant.device.AssistantDatabase
import com.assistant.device.NotificationEntity

class NotificationIntegration(private val context: Context) {
    private val database = AssistantDatabase.getDatabase(context)

    suspend fun getNotificationsContext(sinceTime: Long): String {
        val recentNotifications = try {
            database.notificationDao().getAllLast24Hours(sinceTime)
        } catch (e: Exception) {
            emptyList<NotificationEntity>()
        }

        val whatsappMsgs = recentNotifications.filter { 
            it.packageName.contains("whatsapp", ignoreCase = true) || 
            it.packageName == "com.whatsapp" || 
            it.packageName == "com.whatsapp.w4b"
        }
        val gmailMsgs = recentNotifications.filter { 
            it.packageName.contains("gm", ignoreCase = true) || 
            it.packageName.contains("gmail", ignoreCase = true) ||
            it.packageName == "com.google.android.gm"
        }
        val smsMsgs = recentNotifications.filter { 
            SmsIntegration.isSmsPackage(it.packageName)
        }
        val otherNotifs = recentNotifications.filter { 
            !it.packageName.contains("whatsapp", ignoreCase = true) && 
            !it.packageName.contains("gm", ignoreCase = true) && 
            !it.packageName.contains("gmail", ignoreCase = true) &&
            !SmsIntegration.isSmsPackage(it.packageName) &&
            it.packageName != "com.whatsapp" &&
            it.packageName != "com.whatsapp.w4b" &&
            it.packageName != "com.google.android.gm"
        }

        val sb = StringBuilder()

        sb.append("=== WHATSAPP MESSAGES ===\n")
        if (whatsappMsgs.isEmpty()) {
            sb.append("(No recent WhatsApp messages)\n")
        } else {
            for (msg in whatsappMsgs.take(15)) {
                val timeStr = android.text.format.DateFormat.format("hh:mm a", msg.timestamp)
                val status = if (msg.isReadToUser) "[Read]" else "[NEW]"
                sb.append("- $status [$timeStr] SENDER: ${msg.sender ?: "Unknown"}, TITLE: ${msg.title ?: "None"}, CONTENT: ${msg.text}\n")
            }
        }
        sb.append("\n")

        sb.append("=== GMAIL / EMAIL NOTIFICATIONS ===\n")
        if (gmailMsgs.isEmpty()) {
            sb.append("(No recent email notifications)\n")
        } else {
            for (msg in gmailMsgs.take(15)) {
                val timeStr = android.text.format.DateFormat.format("hh:mm a", msg.timestamp)
                val status = if (msg.isReadToUser) "[Read]" else "[NEW]"
                sb.append("- $status [$timeStr] SENDER: ${msg.sender ?: "Unknown"}, TITLE: ${msg.title ?: "None"}, CONTENT: ${msg.text}\n")
            }
        }
        sb.append("\n")

        sb.append("=== SMS TEXT MESSAGES ===\n")
        if (smsMsgs.isEmpty()) {
            sb.append("(No recent SMS text messages)\n")
        } else {
            for (msg in smsMsgs.take(15)) {
                val timeStr = android.text.format.DateFormat.format("hh:mm a", msg.timestamp)
                val status = if (msg.isReadToUser) "[Read]" else "[NEW]"
                sb.append("- $status [$timeStr] SENDER: ${msg.sender ?: "Unknown"}, CONTENT: ${msg.text}\n")
            }
        }
        sb.append("\n")

        sb.append("=== OTHER NOTIFICATIONS ===\n")
        if (otherNotifs.isEmpty()) {
            sb.append("(No other recent notifications)\n")
        } else {
            for (msg in otherNotifs.take(10)) {
                val timeStr = android.text.format.DateFormat.format("hh:mm a", msg.timestamp)
                val appLabel = getAppNameFromPackage(msg.packageName)
                sb.append("- [$timeStr] APP: $appLabel, SENDER: ${msg.sender ?: "Unknown"}, TITLE: ${msg.title ?: "None"}, CONTENT: ${msg.text}\n")
            }
        }

        return sb.toString().trimEnd()
    }

    private fun getAppNameFromPackage(packageName: String): String {
        val pm = context.packageManager
        return try {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }
    }
}
