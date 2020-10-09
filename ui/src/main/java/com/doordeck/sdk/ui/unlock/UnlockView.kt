package com.doordeck.sdk.ui.unlock

// interface used between the presenter and the activity to make sure
// the presenter does not know about the activity
internal interface UnlockView {
    fun showAccessDenied()

    fun showNoAccessGeoFence()

    fun unlockSuccess()

    /**
     * @param delayOfDevice is used when
     * a device has been unlocked successfully,
     * it could bring a delay and this is communicating
     * to the view that it'll take a bit
     */
    fun unlockSuccessWithDelay(delayOfDevice: Double)

    fun updateLockName(name: String)

    fun setUnlocking()

    fun showGeoLoading()

    fun checkGoogleApiPermissions()

    fun finishActivity()

    fun notValidTileId()

    fun displayVerificationView()

    fun noUserLoggedIn()
}
