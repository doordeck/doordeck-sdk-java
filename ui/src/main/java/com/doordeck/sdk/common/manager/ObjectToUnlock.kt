package com.doordeck.sdk.common.manager

import com.doordeck.sdk.dto.device.Device
import java.util.UUID

sealed class ObjectToUnlock

class DeviceToUnlock(val device: Device) : ObjectToUnlock()

class TileIdToUnlock(val tileID: UUID) : ObjectToUnlock()