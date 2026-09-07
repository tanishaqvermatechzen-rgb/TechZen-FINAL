package com.assistant.device

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SmartDeviceAdapter(
    private val onToggle: (SmartDeviceEntity) -> Unit
) : ListAdapter<SmartDeviceEntity, SmartDeviceAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        val iconContainer: FrameLayout = view.findViewById(R.id.deviceIconContainer)
        val icon: ImageView = view.findViewById(R.id.deviceIcon)
        val name: TextView = view.findViewById(R.id.deviceName)
        val status: TextView = view.findViewById(R.id.deviceStatus)
        val switch: SwitchCompat = view.findViewById(R.id.deviceSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_smart_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = getItem(position)
        holder.name.text = device.name
        holder.status.text = device.statusText
        holder.switch.isChecked = device.isOn

        // Dynamic icon assignment based on device type
        val iconRes = when (device.type) {
            "lock" -> android.R.drawable.ic_lock_idle_lock
            "plug" -> android.R.drawable.ic_menu_preferences
            else -> android.R.drawable.btn_star_big_on // light
        }
        holder.icon.setImageResource(iconRes)

        // Light/Dark Theme Color Tinting based on active state
        if (device.isOn) {
            holder.iconContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0EA5E9")) // Sky Blue
            holder.icon.imageTintList = ColorStateList.valueOf(Color.WHITE)
            holder.status.setTextColor(Color.parseColor("#0284C7"))
        } else {
            holder.iconContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2E8F0")) // Light Grey
            holder.icon.imageTintList = ColorStateList.valueOf(Color.parseColor("#64748B"))
            holder.status.setTextColor(Color.parseColor("#64748B"))
        }

        // Tap triggers onToggle callback
        holder.root.setOnClickListener {
            onToggle(device)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SmartDeviceEntity>() {
        override fun areItemsTheSame(oldItem: SmartDeviceEntity, newItem: SmartDeviceEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SmartDeviceEntity, newItem: SmartDeviceEntity): Boolean {
            return oldItem == newItem
        }
    }
}
