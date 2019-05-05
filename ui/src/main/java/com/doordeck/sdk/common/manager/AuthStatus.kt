package com.doordeck.sdk.common.manager

/**
 * Auth status of the user. The scan is only possible if the user is AUTHORIZED
 */
enum class AuthStatus {
    UNAUTHORIZED,
    AUTHORIZED,
    TWO_FACTOR_AUTH_NEEDED
}
