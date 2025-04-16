package com.doordeck.sdk.ui.showlistofdevicestounlock

import com.doordeck.multiplatform.sdk.model.responses.LockResponse

internal interface ShowListOfDevicesToUnlockView {
    fun showDevices(devices: List<LockResponse>)
}
