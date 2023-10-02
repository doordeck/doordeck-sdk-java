package com.doordeck.sdk.common.models

import com.doordeck.sdk.common.utils.UuidUtils
import com.doordeck.sdk.dto.Role
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.dto.device.DeviceSetting
import com.doordeck.sdk.dto.device.UnlockBetweenWindow
import java.time.Instant
import java.util.Optional
import java.util.UUID

class DefaultDeviceWithUuid(
    private val uuid: String,
    private val name: String = defaultName,
    private val colour: String? = defaultColour,
    private val favourite: Boolean = defaultFavourite,
    private val start: Instant? = defaultStart,
    private val end: Instant? = defaultEnd,
    private val role: Role = defaultRole,
    private val deviceSettings: DeviceSetting = defaultDeviceSettings,
) : Device {
    override fun deviceId(): UUID {
        if (UuidUtils.isUUID(uuid)) {
            return UUID.fromString(uuid)
        }

        throw IllegalStateException("UUID not valid")
    }

    override fun name(): String = name

    override fun colour(): String? = colour

    override fun favourite(): Boolean = favourite

    override fun start(): Optional<Instant> = Optional.ofNullable(start)

    override fun end(): Optional<Instant> = Optional.ofNullable(end)

    override fun role(): Role = role

    override fun settings(): DeviceSetting = deviceSettings

    private companion object {
        private const val defaultName: String = ""
        private val defaultColour: String? = null
        private const val defaultFavourite: Boolean = false
        private val defaultStart: Instant? = null
        private val defaultEnd: Instant? = null
        private val defaultRole: Role = Role.USER
        private val defaultDeviceSettings: DeviceSetting = DefaultDeviceSettings()
    }

    private class DefaultDeviceSettings: DeviceSetting {
        override fun unlockBetween(): Optional<UnlockBetweenWindow> = Optional.empty()

        override fun defaultName(): String = ""

        override fun tiles(): MutableSet<UUID> = mutableSetOf()

    }
}