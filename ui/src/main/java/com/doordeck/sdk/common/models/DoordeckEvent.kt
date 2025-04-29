package com.doordeck.sdk.common.models

import com.doordeck.multiplatform.sdk.model.responses.LocationRequirementResponse

sealed class DoordeckEvent

data class GeneralErrorEvent(val exception: Throwable) : DoordeckEvent()

data class UnlockSuccessEvent(val deviceId: String) : DoordeckEvent()

data class UnlockFailedEvent(val deviceId: String, val exception: Throwable) : DoordeckEvent()

data class ResolveTileSuccess(val tileId: String) : DoordeckEvent()

data class ResolveTileFailed(val tileId: String, val exception: Throwable) : DoordeckEvent()

data class GeofenceError(val deviceId: String, val locationRequirement: LocationRequirementResponse): DoordeckEvent()