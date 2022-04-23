package com.doordeck.sdk.ui.showlistofdevicestounlock

import com.doordeck.sdk.dto.device.Device


internal class ShowListOfDevicesToUnlockPresenter(private val devices: List<Device>) {

    private val TAG = ShowListOfDevicesToUnlockPresenter::class.java.canonicalName


    fun onStart(view: ShowListOfDevicesToUnlockView) {
        view.showDevices(devices)
    }

    fun unlockDevice(device: Device) {

    }
}
