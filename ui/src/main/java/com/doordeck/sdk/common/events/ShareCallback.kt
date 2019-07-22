package com.doordeck.sdk.common.events

/**
 * Interface to implement (but optional) in the showUnlockScreen
 */
interface ShareCallback
{
    fun shareSuccess()
    fun shareFailed()
    fun invalidAuthToken()
    fun notAuthenticated()
}

