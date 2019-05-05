package com.doordeck.sdk.ui.verify

// interface between the presenter and the activity
internal interface VerifyDeviceView  {
    fun succeed()
    fun setEmail(email: String)
    fun setPhoneNumber(phone : String)
    fun noMethodDefined()
}
