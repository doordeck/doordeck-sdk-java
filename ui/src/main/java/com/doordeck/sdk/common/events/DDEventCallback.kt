package com.doordeck.sdk.common.events

/**
 * Callback to implement by the user in order receive the callbacks sent by the SDK
 */
public interface DDEventCallback {
    fun twoFactorAuthNeeded()
    fun noInternet()
    fun invalidAuthToken()
    fun networkError()
    fun emailSent()
    fun emailFailedSending()
    fun codeVerificationSuccess()
    fun codeVerificationFailed()
    fun sdkError()
    fun unlockSuccess()
    fun unlockFailed()
    fun resolveTileFailed()
    fun resolveTileSuccess()
    fun unlockedInvalidTileID()
    fun getCertificateSuccess()
}