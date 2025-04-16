package com.doordeck.sdk.ui.showlistofdevicestounlock.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doordeck.multiplatform.sdk.model.responses.LockResponse
import com.github.doordeck.ui.R
import androidx.core.graphics.toColorInt

class DevicesToUnlockAdapter(
        private val devices: List<LockResponse>,
        private val onDeviceClicked: (LockResponse) -> Unit
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
    fun showDevice(device: LockResponse, onDeviceClicked: (LockResponse) -> Unit) {
        view.findViewById<TextView>(R.id.deviceName)?.text = device.name

        val backgroundColour = device.colour?.toColorInt() ?: ContextCompat.getColor(view.context, R.color.ddColorSecondaryDark)
        (view as CardView).setCardBackgroundColor(backgroundColour)

        view.setOnClickListener {
            onDeviceClicked(device)
        }
    }

}