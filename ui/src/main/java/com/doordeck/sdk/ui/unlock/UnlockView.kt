package com.doordeck.sdk.ui.unlock

import com.doordeck.multiplatform.sdk.model.responses.LocationRequirementResponse
import com.doordeck.multiplatform.sdk.model.responses.LockResponse

// interface used between the presenter and the activity to make sure
// the presenter does not know about the activity
internal interface UnlockView {
    fun showAccessDenied()

    fun showNoAccessGeoFence()

    fun unlockSuccess()

    fun updateLockName(name: String)

    fun setUnlocking()

    fun showGeoLoading()

    fun checkGoogleApiPermissions(device: LockResponse, location: LocationRequirementResponse)

    fun finishActivity()

    fun notValidTileId()

    fun displayVerificationView()

    fun noUserLoggedIn()

    fun goToDevices(devices: List<LockResponse>)

    fun getDefaultLockColours(): Array<String>
}
