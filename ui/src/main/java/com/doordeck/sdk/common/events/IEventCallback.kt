package com.doordeck.sdk.common.events

/**
 * Callback to implement by the user in order receive the callbacks sent by the SDK
 */
interface IEventCallback {
    fun noInternet() // no internet
    fun networkError() // issue when calling the server
    fun verificationCodeSent() // 2fa : new code sent
    fun verificationCodeFailedSending() // 2fa : code failed sending
    fun codeVerificationSuccess() // 2 fa : the code entered is correct, the user is verified
    fun codeVerificationFailed() // 2 fa : the code entered is incorrect
    fun sdkError() // issue in the SDK
    fun unlockSuccess() // the door has been unlocked
    fun unlockFailed() // the door can't be unlocked
    fun resolveTileFailed() // can't resolve the door to open for the scanned device ID
    fun resolveTileSuccess() // door information found from the scanned device ID
    fun unlockedInvalidTileID() // invalid device ID
}