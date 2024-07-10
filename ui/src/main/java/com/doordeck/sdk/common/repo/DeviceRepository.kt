package com.doordeck.sdk.common.repo

import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.http.service.DeviceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.retrofit.Result
import ru.gildor.coroutines.retrofit.awaitResult
import java.util.*

interface DeviceRepository {
    suspend fun getDevicesAvailable(tileId: String, defaultLockColours: Array<String>): Result<List<Device>>
}

class DeviceRepositoryImpl(
    private val deviceService: DeviceService
) : DeviceRepository {
    override suspend fun getDevicesAvailable(tileId: String, defaultLockColours: Array<String>): Result<List<Device>> {
        return when (val devices = deviceService.resolveTile(UUID.fromString(tileId)).awaitResult()) {
            is Result.Ok -> {
                devices.value.deviceIds().map {
                    withContext(Dispatchers.Default) {
                        val deviceResult = deviceService.getDevice(it).awaitResult()
                        if (deviceResult is Result.Ok && deviceResult.value.colour() == null) {
                            updateColourOfDevice(deviceResult.value.deviceId(), defaultLockColours.random())
                        }
                        return@withContext deviceService.getDevice(it).awaitResult()
                    }
                }
                    .filterIsInstance<Result.Ok<Device>>()
                    .map { return@map it.value }
                    .let { return@let Result.Ok(it, devices.response) }
            }
            is Result.Error -> return devices
            is Result.Exception -> return devices
        }
    }

    private suspend fun updateColourOfDevice(deviceId: UUID, colour: String): Result<Void> {
        return deviceService.updateDevice(
            deviceId,
            "{\"colour\": \"$colour\"}"
                .toRequestBody("application/json".toMediaType())
        ).awaitResult()
    }
}
