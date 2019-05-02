package com.doordeck.sdk.common.events

/**
 * Callback to implement by the user in order receive the callbacks sent by the SDK
 */
interface IEventCallback {
    fun noInternet()
    fun networkError()
    fun verificationCodeSent()
    fun verificationCodeFailedSending()
    fun codeVerificationSuccess()
    fun codeVerificationFailed()
    fun sdkError()
    fun unlockSuccess()
    fun unlockFailed()
    fun resolveTileFailed()
    fun resolveTileSuccess()
    fun unlockedInvalidTileID()
    fun authentificationSuccess()
}