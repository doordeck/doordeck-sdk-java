package com.doordeck.sdk.ui.showlistofdevicestounlock

import com.doordeck.multiplatform.sdk.model.responses.LockResponse

internal class ShowListOfDevicesToUnlockPresenter(private val devices: List<LockResponse>) {
    fun onStart(view: ShowListOfDevicesToUnlockView) {
        view.showDevices(devices)
    }
}
