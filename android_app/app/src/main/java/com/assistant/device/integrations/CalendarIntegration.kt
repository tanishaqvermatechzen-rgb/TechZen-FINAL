package com.assistant.device.integrations

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.ArrayList

class CalendarIntegration(private val context: Context) {
    private val TAG = "CalendarIntegration"

    fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getUpcomingEventsContext(hours: Int = 36): String {
        if (!isPermissionGranted()) {
            return "(Calendar access disabled: READ_CALENDAR permission not granted in phone settings)"
        }

        val now = System.currentTimeMillis()
        val endOfPeriod = now + hours * 60 * 60 * 1000L
        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.EVENT_LOCATION
        )
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, now)
        ContentUris.appendId(builder, endOfPeriod)

        val events = ArrayList<String>()
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val title = cursor.getString(0) ?: "No Title"
                    val begin = cursor.getLong(1)
                    val end = cursor.getLong(2)
                    val location = cursor.getString(3) ?: "No Location"
                    val timeStr = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a", begin).toString()
                    val durationMin = (end - begin) / (60 * 1000)
                    events.add("- $title ($timeStr, duration: ${durationMin}m, location: $location)")
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying calendar instances", e)
            return "(Calendar query error: ${e.message})"
        } finally {
            cursor?.close()
        }

        return if (events.isEmpty()) {
            "(No upcoming calendar events for the next $hours hours)"
        } else {
            events.joinToString("\n")
        }
    }
}
