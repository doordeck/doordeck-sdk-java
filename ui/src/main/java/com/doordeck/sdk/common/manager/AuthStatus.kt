package com.doordeck.sdk.common.manager

import com.doordeck.multiplatform.sdk.Doordeck

/**
 * Auth status of the user. The scan is only possible if the user is AUTHORIZED
 */
enum class AuthStatus {
    UNAUTHORIZED,
    AUTHORIZED,
    TWO_FACTOR_AUTH_NEEDED
}

val Doordeck.authStatus: AuthStatus
    get() = if (contextManager().isCloudAuthTokenAboutToExpire()) {
        AuthStatus.UNAUTHORIZED
    } else if (contextManager().isCertificateChainAboutToExpire()) {
        AuthStatus.TWO_FACTOR_AUTH_NEEDED
    } else {
        AuthStatus.AUTHORIZED
    }