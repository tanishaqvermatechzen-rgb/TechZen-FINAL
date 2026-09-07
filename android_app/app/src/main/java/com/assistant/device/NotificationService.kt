package com.assistant.device

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService : NotificationListenerService() {
    private val TAG = "NotificationService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var database: AssistantDatabase

    override fun onCreate() {
        super.onCreate()
        database = AssistantDatabase.getDatabase(applicationContext)
        Log.i(TAG, "Notification Listener Service created.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        
        // Exclude system UI
        if (packageName == "com.android.systemui") return

        var title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        var text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        
        // Handle MessagingStyle (WhatsApp, Telegram, etc.)
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        if (messages != null && messages.isNotEmpty()) {
            val lastMessage = messages.last() as? android.os.Bundle
            if (lastMessage != null) {
                val messageText = lastMessage.getCharSequence("text")?.toString()
                val messageSender = lastMessage.getCharSequence("sender")?.toString()
                if (!messageText.isNullOrEmpty()) {
                    text = messageText
                    if (!messageSender.isNullOrEmpty()) {
                        title = messageSender
                    }
                }
            }
        }
 else {
            // Fallback to BigText if available
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            if (!bigText.isNullOrEmpty()) {
                text = bigText
            }
        }

        // Final check: if still empty, ignore
        if (text.isEmpty() && title.isEmpty()) return

        // Resolve Sender
        val conversationTitle = extras.getString(Notification.EXTRA_CONVERSATION_TITLE)
        val sender = if (!conversationTitle.isNullOrEmpty()) conversationTitle else title

        // Check if user has configured an app notification filter whitelist
        val sharedPrefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE)
        val selectedApps = sharedPrefs.getStringSet("selected_notification_apps", null)
        if (selectedApps != null && selectedApps.isNotEmpty() && !selectedApps.contains(packageName)) {
            Log.d(TAG, "Skipping notification from unselected app: $packageName")
            return
        }

        val sbnKey = sbn.key ?: ""
        Log.d(TAG, "Notification received: App=$packageName, Sender=$sender, Key=$sbnKey, Message=$text")

        serviceScope.launch {
            // Deduplication: Check if identical notification was saved in the last 15 minutes
            val recentDup = database.notificationDao().getRecentByKeyOrText(
                key = sbnKey,
                pkg = packageName,
                text = text,
                sinceTime = System.currentTimeMillis() - 15 * 60 * 1000L
            )

            if (recentDup != null) {
                Log.d(TAG, "Skipping duplicate notification for key: $sbnKey")
                return@launch
            }

            // Save to Local Database
            val entity = NotificationEntity(
                packageName = packageName,
                sender = sender,
                title = title,
                text = text,
                timestamp = System.currentTimeMillis(),
                notificationKey = sbnKey
            )
            database.notificationDao().insert(entity)
            
            // Clean up notifications older than 24 hours
            val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
            database.notificationDao().deleteOldNotifications(cutoff)

            // Trigger BluetoothService to evaluate whether to start buzzing the device
            val intent = Intent(applicationContext, BluetoothService::class.java).apply {
                action = BluetoothService.ACTION_EVALUATE_BUZZ
            }
            startService(intent)
        }
    }


    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Optional: handle dismissed notifications
    }
}
