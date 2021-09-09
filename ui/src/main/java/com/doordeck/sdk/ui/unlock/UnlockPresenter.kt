package com.doordeck.sdk.ui.unlock

import android.app.Activity
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.manager.AuthStatus
import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.common.services.LocationService
import com.doordeck.sdk.common.utils.LOG
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.dto.operation.ImmutableMutateDoorState
import com.doordeck.sdk.http.DoordeckClient
import com.doordeck.sdk.common.repo.DeviceRepository
import com.doordeck.sdk.common.repo.DeviceRepositoryImpl
import com.doordeck.sdk.signer.util.JWTUtils
import com.doordeck.sdk.ui.showlistofdevicestounlock.ShowListOfDevicesToUnlockActivity
import kotlinx.coroutines.*
import retrofit2.Response
import ru.gildor.coroutines.retrofit.Result
import ru.gildor.coroutines.retrofit.awaitResponse
import java.util.*


// logic related to the view responsible to unlock the device
internal class UnlockPresenter {

    private val TAG = UnlockPresenter::class.java.canonicalName
    private val CLOSE_APP_DELAY_MS = 5000L


    private var locationService: LocationService? = null
    private var deviceToUnlock: Device? = null
    private var jobs: List<Job> = emptyList()
    private var client: DoordeckClient? = null
    private var deviceRepository: DeviceRepository? = null
    private var view: UnlockView? = null

    fun onStart(view: UnlockView) {
        this.view = view
        this.locationService = LocationService(view as Activity)

    }

    /**
     * clean memory
     */
    fun onStop() {
        jobs.forEach { it.cancel() }
        this.view = null
        this.locationService = null
    }


    /**
     * Initialize the presenter with the tile_id
     * @param tileId id of the tile scanned
     */
    fun init(tileId: String?) {


        // wait for certificate to be loaded if nescesarry
        if (Doordeck.certificateChain == null && Doordeck.status != AuthStatus.TWO_FACTOR_AUTH_NEEDED) {
            Doordeck.onCertLoaded = { oldValue, newValue ->
                if (newValue == true) initUnlock(tileId)
            }
        } else {
            initUnlock(tileId)
        }

    }

    /**
     * Initialize the presenter with the device
     * @param device of the pressed button
     */
    fun init(device: Device?) {

        // wait for certificate to be loaded if nescesarry
        if (Doordeck.certificateChain == null && Doordeck.status != AuthStatus.TWO_FACTOR_AUTH_NEEDED) {
            Doordeck.onCertLoaded = { oldValue, newValue ->
                if (newValue == true) initUnlock(device)
            }
        } else {
            initUnlock(device)
        }

    }

    private fun initUnlock(tileId: String?) {

        if (Doordeck.client != null) {
            this.client = Doordeck.client
            this.deviceRepository = DeviceRepositoryImpl(client!!.device())
        }

        if (tileId == null) {
            EventsManager.sendEvent(EventAction.UNLOCK_INVALID_TILE_ID)
            view?.notValidTileId()
            return
        }

        if (Doordeck.status == AuthStatus.TWO_FACTOR_AUTH_NEEDED) {
            view?.displayVerificationView()
            return
        }

        var certif = Doordeck.certificateChain

        certif?.let { resolveTile(tileId) }
    }

    private fun initUnlock(device: Device?) {

        if (Doordeck.client != null) {
            this.client = Doordeck.client
        }

        if (device == null) {
            EventsManager.sendEvent(EventAction.RESOLVE_TILE_FAILED)
            view?.notValidTileId()
            return
        }

        if (Doordeck.status == AuthStatus.TWO_FACTOR_AUTH_NEEDED) {
            view?.displayVerificationView()
            return
        }

        var certif = Doordeck.certificateChain

        certif?.let { resolveTileSuccess(device) }
    }


    /**
     * Call the server to get the detail of the device given the tileId
     * @param tileId id of the tile scanned
     */
    private fun resolveTile(tileId: String) {
        jobs += GlobalScope.launch(Dispatchers.Main) {
            when(val result = deviceRepository?.getDevicesAvailable(tileId, view?.getDefaultLockColours() ?: arrayOf())) {
                is Result.Ok -> proceedWithDevices(result.value)
                is Result.Error -> {
                    accessDenied()
                    EventsManager.sendEvent(EventAction.RESOLVE_TILE_FAILED, result.exception)
                    LOG.e(TAG, "resolveTile err : " + result.exception)
                }
                is Result.Exception -> {
                    EventsManager.sendEvent(EventAction.SDK_NETWORK_ERROR, result.exception)
                    LOG.e(TAG, "resolveTile Something broken : " + result.exception)
                }
                null -> {
                    EventsManager.sendEvent(EventAction.RESOLVE_TILE_FAILED, "instance is null, development problem")
                    LOG.e(TAG, "instance is null")
                }
            }
        }
    }

    private fun proceedWithDevices(devices: List<Device>) {
        when {
            devices.isEmpty() -> accessDenied()
            devices.size == 1 -> resolveTileSuccess(devices.first())
            else -> view?.goToDevices(devices)
        }
    }

    private fun resolveTileSuccess(device: Device) {
        EventsManager.sendEvent(EventAction.RESOLVE_TILE_SUCCESS)
        this.deviceToUnlock = device
        view?.updateLockName(device.name())
        view?.setUnlocking()

        if (device.settings().usageRequirements().location().isPresent
                && device.settings().usageRequirements().location().get().enabled()) {
            view?.showGeoLoading()
            view?.checkGoogleApiPermissions()
        } else {
            unlockDevice(
                    deviceId = device.deviceId(),
                    delayOfDevice = device.settings().delay()
            )
        }
    }


    /**
     * Display the acces denied animation
     */
    private fun accessDenied() {
        view?.showAccessDenied()
    }

    /**
     * Call the server unlock the device
     * @param deviceId UUID of the device to open
     * @param delayOfDevice is the delay that the Device has shown it'll take to load
     */
    private fun unlockDevice(deviceId: UUID, delayOfDevice: Double = 0.0) {
        Doordeck.certificateChain?.let { chain ->
            jobs += GlobalScope.launch(Dispatchers.Main) {
                val signedJWT = JWTUtils.getSignedJWT(chain.certificateChain(),
                        Doordeck.getKeys().private,
                        deviceId,
                        chain.userId(),
                        ImmutableMutateDoorState.builder().locked(false).build()
                )

                val result: Response<Void> = client!!.device().executeOperation(deviceId, signedJWT).awaitResponse()
                when (result.isSuccessful) {
                    true -> {
                        if (delayOfDevice > 0.0) {
                            view?.unlockSuccessWithDelay(delayOfDevice)
                        } else {
                            view?.unlockSuccess()
                        }

                        GlobalScope.launch(Dispatchers.Main) {
                            delay((delayOfDevice * 1000).toLong())
                            EventsManager.sendEvent(EventAction.UNLOCK_SUCCESS)
                        }
                    }
                    false -> {
                        EventsManager.sendEvent(EventAction.UNLOCK_FAILED, result.message())
                        LOG.e(TAG, "Error result : " + result.message())
                        accessDenied()
                    }
                }
            }
        }
    }


    /**
     * Check if the device if around the user
     */
    fun checkGeofence() {

        deviceToUnlock?.let { lock ->
            locationService?.let { locService ->
                val locationRequirement = lock.settings().usageRequirements().location().get()

                locService.getLocation(locationRequirement.accuracy(), object : LocationService.Callback {
                    override fun onGetLocation(lat: Double, lng: Double, acc: Float) {
                        if (locService.inGeofence(locationRequirement.latitude(), locationRequirement.longitude(), locationRequirement.accuracy(), locationRequirement.radius(), lat, lng, acc)) {
                            unlockDevice(lock.deviceId())
                        } else {
                            view?.showNoAccessGeoFence()
                        }

                    }

                    override fun onError(error: String) {

                    }
                })
            }
        }
    }

    /**
     * Close the activity after 5sec
     */
    fun setFinishTimer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                view?.finishActivity()
            }
        }, CLOSE_APP_DELAY_MS)
    }
}
