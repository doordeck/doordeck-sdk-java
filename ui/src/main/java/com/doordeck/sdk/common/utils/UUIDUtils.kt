package com.doordeck.sdk.common.utils

import java.util.UUID

fun isUUID(id: String): Boolean {
    return try {
        UUID.fromString(id)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}