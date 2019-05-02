package com.doordeck.sdk.common.events

interface UnlockCallback
{
    fun unlockSuccess()
    fun unlockFailed()
    fun invalidAuthToken()
    fun notAuthenticated()
}

