package com.doordeck.sdk.ui.showlistofdevicestounlock.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doordeck.sdk.R
import com.doordeck.sdk.dto.device.Device

class DevicesToUnlockAdapter(
        private val devices: List<Device>,
        private val onDeviceClicked: (Device) -> Unit
) : RecyclerView.Adapter<DeviceToUnlockViewHolder>() {
    override fun onCreateViewHolder(root: ViewGroup, position: Int): DeviceToUnlockViewHolder {
        return DeviceToUnlockViewHolder(
                LayoutInflater.from(root.context).inflate(R.layout.lock_to_unlock_item, root, false)
        )
    }

    override fun onBindViewHolder(deviceViewHolder: DeviceToUnlockViewHolder, position: Int) {
        deviceViewHolder.showDevice(devices[position], onDeviceClicked)
    }

    override fun getItemCount() = devices.size

}

class DeviceToUnlockViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun showDevice(device: Device, onDeviceClicked: (Device) -> Unit) {
        view.findViewById<TextView>(R.id.deviceName)?.text = device.name()

        val backgroundColour = device.colour()?.let { Color.parseColor(it) } ?: ContextCompat.getColor(view.context, R.color.ddColorSecondaryDark)
        (view as CardView).setCardBackgroundColor(backgroundColour)

        view.setOnClickListener {
            onDeviceClicked(device)
        }
    }

}