package com.assistant.device

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.ConcurrentHashMap

class NotificationAdapter : ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(DiffCallback) {

    // Cache icons and app names to avoid loading them repeatedly on scroll
    private val iconCache = ConcurrentHashMap<String, Drawable>()
    private val nameCache = ConcurrentHashMap<String, String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val senderText: TextView = view.findViewById(R.id.senderText)
        val timeText: TextView = view.findViewById(R.id.timeText)
        val messageText: TextView = view.findViewById(R.id.messageText)
        val appNameText: TextView = view.findViewById(R.id.appNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context

        // Bind sender/title
        val sender = item.sender
        val title = item.title
        
        holder.senderText.text = when {
            !sender.isNullOrEmpty() && !title.isNullOrEmpty() && sender != title -> "$sender ($title)"
            !sender.isNullOrEmpty() -> sender
            !title.isNullOrEmpty() -> title
            else -> "Notification"
        }

        // Bind message text
        holder.messageText.text = item.text

        // Bind formatted relative time
        holder.timeText.text = getRelativeTimeString(item.timestamp)

        // Resolve App Name and App Icon
        val (appName, appIcon) = getAppDetails(context, item.packageName)
        holder.appNameText.text = appName
        if (appIcon != null) {
            holder.appIcon.setImageDrawable(appIcon)
        } else {
            // Default generic icon fallback (built-in android icon)
            holder.appIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }

    private fun getAppDetails(context: Context, packageName: String): Pair<String, Drawable?> {
        val cachedName = nameCache[packageName]
        val cachedIcon = iconCache[packageName]

        if (cachedName != null && cachedIcon != null) {
            return Pair(cachedName, cachedIcon)
        }

        val pm = context.packageManager
        var appName = packageName.substringAfterLast('.')
        var appIcon: Drawable? = null

        try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            appName = pm.getApplicationLabel(appInfo).toString()
            appIcon = pm.getApplicationIcon(packageName)

            // Cache it
            nameCache[packageName] = appName
            iconCache[packageName] = appIcon
        } catch (e: PackageManager.NameNotFoundException) {
            // Ignore
        }

        return Pair(appName, appIcon)
    }

    private fun getRelativeTimeString(timeMs: Long): CharSequence {
        val now = System.currentTimeMillis()
        // If it's within 1 minute, say "Just now"
        return if (now - timeMs < 60000) {
            "Just now"
        } else {
            DateUtils.getRelativeTimeSpanString(
                timeMs,
                now,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem == newItem
        }
    }
}
