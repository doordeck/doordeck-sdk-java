package com.doordeck.sdk.ui.unlock

// interface used between the presenter and the activity to make sure
// the presenter does not know about the activity
internal interface UnlockView {
    fun showAccessDenied()

    fun showNoAccessGeoFence()

    fun unlockSuccess()

    fun updateLockName(name: String)

    fun setUnlocking()

    fun showGeoLoading()

    fun checkGoogleApiPermissions()

    fun finishActivity()

    fun notValidTileId()

    fun displayVerificationView()
}
