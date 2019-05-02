package com.doordeck.sdk.ui.verify

internal interface VerifyDeviceView  {
    fun succeed()
    fun setEmail(email: String)
    fun setPhoneNumber(phone : String)
    fun noMethodDefined()
}
