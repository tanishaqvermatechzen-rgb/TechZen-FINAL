package com.assistant.device.integrations

import android.content.Context
import com.assistant.device.AssistantDatabase
import com.assistant.device.NotificationEntity

class SmsIntegration(private val context: Context) {
    private val database = AssistantDatabase.getDatabase(context)

    companion object {
        val SMS_PACKAGE_NAMES = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
            "com.sonyericsson.conversations",
            "com.htc.sense.mms"
        )

        fun isSmsPackage(packageName: String): Boolean {
            return SMS_PACKAGE_NAMES.contains(packageName) || 
                   packageName.contains(".messaging", ignoreCase = true) ||
                   packageName.endsWith(".mms", ignoreCase = true)
        }
    }

    suspend fun getRecentSmsNotifications(sinceTime: Long): List<NotificationEntity> {
        val allNotifications = database.notificationDao().getAllLast24Hours(sinceTime)
        return allNotifications.filter { isSmsPackage(it.packageName) }
    }

    suspend fun getFormattedSmsContext(sinceTime: Long): String {
        val smsNotifs = getRecentSmsNotifications(sinceTime)
        if (smsNotifs.isEmpty()) {
            return "(No recent SMS text messages received)"
        }
        val sb = StringBuilder()
        for (msg in smsNotifs.take(15)) {
            val timeStr = android.text.format.DateFormat.format("hh:mm a", msg.timestamp)
            val status = if (msg.isReadToUser) "[Read]" else "[NEW]"
            sb.append("- $status [$timeStr] SENDER: ${msg.sender ?: "Unknown"}, CONTENT: ${msg.text}\n")
        }
        return sb.toString().trimEnd()
    }
}
