package com.doordeck.sdk.ui.verify

// interface between the presenter and the activity
internal interface VerifyDeviceView  {
    fun succeed()
    fun fail()
    fun setEmail(email: String)
    fun setPhoneNumber(phone : String)
    fun setPhoneNumberWhatsapp(phone : String)
    fun noMethodDefined()
    fun verifyCodeSuccess()
    fun verifyCodeFail()
}
