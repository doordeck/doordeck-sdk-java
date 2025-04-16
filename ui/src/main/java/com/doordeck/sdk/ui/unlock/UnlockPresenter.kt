package com.doordeck.sdk.ui.unlock

import android.app.Activity
import com.doordeck.multiplatform.sdk.model.responses.LocationRequirementResponse
import com.doordeck.multiplatform.sdk.model.responses.LockResponse
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.manager.AuthStatus
import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.manager.authStatus
import com.doordeck.sdk.common.models.GeneralErrorEvent
import com.doordeck.sdk.common.models.GeofenceError
import com.doordeck.sdk.common.models.ResolveTileFailed
import com.doordeck.sdk.common.models.ResolveTileSuccess
import com.doordeck.sdk.common.models.UnlockFailedEvent
import com.doordeck.sdk.common.models.UnlockSuccessEvent
import com.doordeck.sdk.common.repo.DeviceRepository
import com.doordeck.sdk.common.repo.DeviceRepositoryImpl
import com.doordeck.sdk.common.services.LocationService
import com.doordeck.sdk.common.utils.LOG
import com.google.android.gms.location.Geofence
import java.util.Timer
import java.util.TimerTask


// logic related to the view responsible to unlock the device
internal class UnlockPresenter {


    private lateinit var locationService: LocationService
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var view: UnlockView

    fun onStart(view: UnlockView) {
        this.view = view
        this.locationService = LocationService(view as Activity)
        this.deviceRepository = DeviceRepositoryImpl(Doordeck.getHeadlessInstance())
    }

    /**
     * Call the server to get the detail of the device given the tileId
     * @param tileId id of the tile scanned
     */
    fun resolveTile(tileId: String) {
        deviceRepository.getDevicesAvailable(tileId, view.getDefaultLockColours())
            .thenApply {
                EventsManager.send(ResolveTileSuccess(tileId = tileId))
                proceedWithDevices(tileId = tileId, devices = it)
            }
            .exceptionally {
                EventsManager.send(ResolveTileFailed(tileId = tileId, exception = it))
                accessDenied()
            }
    }

    private fun proceedWithDevices(tileId: String, devices: List<LockResponse>) {
        when {
            devices.isEmpty() -> {
                EventsManager.send(
                    ResolveTileFailed(
                        exception = Exception("No devices found"),
                        tileId = tileId,
                    )
                )
                accessDenied()
            }

            devices.size == 1 -> {
                EventsManager.send(ResolveTileSuccess(tileId = tileId))
                resolveTileSuccess(devices.first())
            }

            else -> view.goToDevices(devices)
        }
    }

    private fun resolveTileSuccess(device: LockResponse) {
        view.updateLockName(device.name)
        view.setUnlocking()

        val possibleLocation = device.settings.usageRequirements?.location
        if (possibleLocation != null && possibleLocation.enabled == true) {
            view.showGeoLoading()
            view.checkGoogleApiPermissions(device, possibleLocation)
        } else {
            unlockDevice(
                deviceId = device.id,
            )
        }
    }


    /**
     * Display the access denied animation
     */
    private fun accessDenied() {
        view.showAccessDenied()
    }

    /**
     * Call the server unlock the device
     * @param deviceId UUID of the device to open
     */
    private fun unlockDevice(deviceId: String) {
        deviceRepository.unlockDevice(deviceId)
            .thenApply {
                view.unlockSuccess()
                EventsManager.send(UnlockSuccessEvent(deviceId = deviceId))
            }
            .exceptionally {
                EventsManager.send(UnlockFailedEvent(deviceId = deviceId, exception = it))
                accessDenied()
            }
    }


    /**
     * Check if the device if around the user
     */
    fun checkGeofence(deviceToUnlock: LockResponse, locationRequirement: LocationRequirementResponse) {
        locationService.getLocation(locationRequirement.accuracy!! /*TODO*/, object : LocationService.Callback {
            override fun onGetLocation(lat: Double, lng: Double, acc: Float) {
                if (locationService.inGeofence(
                        locationRequirement.latitude,
                        locationRequirement.longitude,
                        locationRequirement.accuracy!! /*TODO*/,
                        locationRequirement.radius!! /*TODO*/,
                        lat,
                        lng,
                        acc
                    )
                ) {
                    unlockDevice(deviceToUnlock.id)
                } else {
                    view.showNoAccessGeoFence()
                    EventsManager.send(
                        GeofenceError(deviceToUnlock.id, locationRequirement = locationRequirement)
                    )
                }

            }

            override fun onError(error: String) {
                EventsManager.send(
                    GeneralErrorEvent(exception = Exception(error))
                )
            }
        })
    }

    /**
     * Close the activity after 5sec
     */
    fun setFinishTimer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                view.finishActivity()
            }
        }, CLOSE_APP_DELAY_MS)
    }

    companion object {
        private val TAG = UnlockPresenter::class.java.name
        private const val CLOSE_APP_DELAY_MS = 5000L
    }
}
