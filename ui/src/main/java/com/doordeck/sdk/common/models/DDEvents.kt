package com.doordeck.sdk.common.models

// events sent by the SDK
data class DDEVENT(val event: EventAction, val exception: String? = null)

enum class EventAction {
    NO_INTERNET,
    SDK_ERROR,
    SDK_NETWORK_ERROR,
    VERIFICATION_CODE_SENT,
    VERIFICATION_CODE_FAILED_SENDING,
    CODE_VERIFICATION_SUCCESS,
    CODE_VERIFICATION_FAILED,
    UNLOCK_INVALID_TILE_ID,
    UNLOCK_SUCCESS,
    UNLOCK_FAILED,
    RESOLVE_TILE_FAILED,
    RESOLVE_TILE_SUCCESS
}