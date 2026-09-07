package com.assistant.device

import android.content.Context
import com.assistant.device.integrations.CalendarIntegration
import com.assistant.device.integrations.MediaIntegration
import com.assistant.device.integrations.NotificationIntegration

class AiGateway(private val context: Context) {
    private val calendarIntegration = CalendarIntegration(context)
    private val mediaIntegration = MediaIntegration(context)
    private val notificationIntegration = NotificationIntegration(context)

    suspend fun gatherGatewayContext(): String {
        val now = System.currentTimeMillis()
        val currentDateStr = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a, EEEE", now).toString()
        val sinceTime = now - 24 * 60 * 60 * 1000L

        // Hard 24-hour retention enforcement: Purge records older than 24 hours from disk
        val database = AssistantDatabase.getDatabase(context)
        try {
            database.notificationDao().deleteOldNotifications(sinceTime)
            database.voiceChatDao().deleteOldVoiceChats(sinceTime)
        } catch (e: Exception) {
            android.util.Log.e("AiGateway", "Error purging 24h old data", e)
        }

        val mediaContext = mediaIntegration.getMediaStatusContext()
        val notificationsContext = notificationIntegration.getNotificationsContext(sinceTime)
        val calendarContext = calendarIntegration.getUpcomingEventsContext(36)

        // Gather Smart Device Context
        val smartDevices = try {
            database.smartDeviceDao().getAllDevicesDirect()
        } catch (e: Exception) {
            emptyList<SmartDeviceEntity>()
        }
        val smartDeviceContext = if (smartDevices.isEmpty()) {
            "(No smart devices configured)"
        } else {
            smartDevices.joinToString("\n") { device ->
                "- ${device.name} (${device.type}): ${if (device.isOn) "ON" else "OFF"}${if (device.value != 0) ", Value: ${device.value}" else ""}${if (device.statusText.isNotEmpty()) ", Status: ${device.statusText}" else ""}"
            }
        }

        val contextBuilder = StringBuilder()
        contextBuilder.append("Current Time: $currentDateStr\n\n")

        contextBuilder.append("=== MEDIA STATUS ===\n")
        contextBuilder.append(mediaContext)
        contextBuilder.append("\n\n")

        contextBuilder.append(notificationsContext)
        contextBuilder.append("\n\n")

        contextBuilder.append("=== CALENDAR EVENTS (Next 36 Hours) ===\n")
        contextBuilder.append(calendarContext)
        contextBuilder.append("\n\n")

        contextBuilder.append("=== SMART DEVICES ===\n")
        contextBuilder.append(smartDeviceContext)
        contextBuilder.append("\n\n")

        contextBuilder.append("=== SYSTEM STATUS ===\n")
        contextBuilder.append("- Phone Gateway: Linked & Active\n")

        return contextBuilder.toString()
    }
}
