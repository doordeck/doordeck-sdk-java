package com.doordeck.sdk.common.manager

import com.doordeck.multiplatform.sdk.model.responses.LockResponse
import java.util.UUID

sealed class ObjectToUnlock

class DeviceToUnlock(val device: LockResponse) : ObjectToUnlock()

class TileIdToUnlock(val tileID: UUID) : ObjectToUnlock()