package com.doordeck.sdk.common.events

/**
 * Interface to implement (but optional) in the showUnlockScreen
 */
interface UnlockCallback
{
    fun unlockSuccess()
    fun unlockFailed()
    fun invalidAuthToken()
    fun notAuthenticated()
}

