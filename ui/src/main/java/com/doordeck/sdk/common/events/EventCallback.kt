package com.doordeck.sdk.common.events

/**
 * Callback to implement by the user in order receive the callbacks sent by the SDK
 */


abstract class EventCallback : IEventCallback {
    override fun noInternet() {}
    override fun networkError() {}
    override fun verificationCodeSent() {}
    override fun verificationCodeFailedSending() {}
    override fun codeVerificationSuccess() {}
    override fun codeVerificationFailed() {}
    override fun sdkError() {}
    override fun unlockSuccess() {}
    override fun unlockFailed() {}
    override fun resolveTileFailed() {}
    override fun resolveTileSuccess() {}
    override fun unlockedInvalidTileID() {}
}