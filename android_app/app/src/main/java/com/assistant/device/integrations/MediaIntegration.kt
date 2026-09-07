package com.assistant.device.integrations

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log

class MediaIntegration(private val context: Context) {
    private val TAG = "MediaIntegration"

    fun getMediaStatusContext(): String {
        val mediaManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            ?: return "(MediaSession service unavailable)"

        val componentName = ComponentName(context, "com.assistant.device.NotificationService")

        try {
            val controllers = mediaManager.getActiveSessions(componentName)
            if (controllers.isEmpty()) {
                return "(No active media sessions playing)"
            }

            val sb = StringBuilder()
            for (controller in controllers) {
                val metadata = controller.metadata
                val packageName = controller.packageName
                val appLabel = getAppNameFromPackage(packageName)

                val playbackStateStr = when (controller.playbackState?.state) {
                    PlaybackState.STATE_PLAYING -> "Playing"
                    PlaybackState.STATE_PAUSED -> "Paused"
                    PlaybackState.STATE_BUFFERING -> "Buffering"
                    PlaybackState.STATE_STOPPED -> "Stopped"
                    else -> "Idle"
                }

                if (metadata != null) {
                    val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown Title"
                    val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                    val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
                    sb.append("- App: $appLabel, Title: \"$title\", Artist: $artist ${if (album.isNotEmpty()) "[$album]" else ""} ($playbackStateStr)\n")
                } else {
                    sb.append("- App: $appLabel ($playbackStateStr)\n")
                }
            }
            return sb.toString().trimEnd()
        } catch (e: Exception) {
            Log.d(TAG, "MediaSession active sessions check failed: ${e.message}")
            return "(Media status context unavailable - requires Notification Access in Settings)"
        }
    }

    fun executeMediaControl(action: String): Boolean {
        val mediaManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            ?: return false
        val componentName = ComponentName(context, "com.assistant.device.NotificationService")

        try {
            val controllers = mediaManager.getActiveSessions(componentName)
            if (controllers.isEmpty()) return false

            val primaryController = controllers.firstOrNull { 
                it.playbackState?.state == PlaybackState.STATE_PLAYING 
            } ?: controllers.first()

            val controls: MediaController.TransportControls = primaryController.transportControls
            when (action.lowercase()) {
                "play" -> controls.play()
                "pause" -> controls.pause()
                "play_pause", "toggle" -> {
                    if (primaryController.playbackState?.state == PlaybackState.STATE_PLAYING) {
                        controls.pause()
                    } else {
                        controls.play()
                    }
                }
                "next", "skip" -> controls.skipToNext()
                "previous", "prev" -> controls.skipToPrevious()
                "stop" -> controls.stop()
                else -> return false
            }
            Log.i(TAG, "Executed MediaControl '$action' on app: ${primaryController.packageName}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute MediaControl '$action'", e)
            return false
        }
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
