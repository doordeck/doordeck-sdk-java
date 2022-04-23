package com.doordeck.sdk.ui.showlistofdevicestounlock

import com.doordeck.sdk.dto.device.Device

internal interface ShowListOfDevicesToUnlockView {
    fun showDevices(devices: List<Device>)
}
