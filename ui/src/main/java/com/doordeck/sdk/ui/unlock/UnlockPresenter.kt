package com.doordeck.sdk.ui.unlock

import android.app.Activity
import android.util.Log
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.manager.DoordeckSDK
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.common.services.LocationService
import com.doordeck.sdk.common.utils.LOG
import com.doordeck.sdk.dto.certificate.CertificateChain
import com.doordeck.sdk.dto.certificate.ImmutableRegisterEphemeralKey
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.signer.util.JWTSignedUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.gildor.coroutines.retrofit.Result
import ru.gildor.coroutines.retrofit.awaitResponse
import ru.gildor.coroutines.retrofit.awaitResult
import java.util.*


// logic related to the view responsible to unlock the device
internal class UnlockPresenter {

    private val TAG = UnlockPresenter::class.java.canonicalName
    private val CLOSE_APP_DELAY_MS = 5000L


    private var locationService: LocationService? = null
    private var deviceToUnlock: Device? = null
    private var jobs: List<Job> = emptyList()
    private val client = DoordeckSDK.client
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

        if (tileId == null) {
            EventsManager.sendEvent(EventAction.UNLOCK_INVALID_TILE_ID)
            view?.notValidTileId()
            return
        }

        val certif = DoordeckSDK.certificateChain
        certif?.let {
            DoordeckSDK.certificateChain = it
            resolveTile(tileId)
        } ?:
        getCertificate(tileId)
    }

    /**
     * Call the server to get the certificate given the user's ephemeral key
     * @param tileId id of the tile scanned
     */
    private fun getCertificate(tileId: String) {
        jobs += GlobalScope.launch(Dispatchers.Main) {
            val ephKey = ImmutableRegisterEphemeralKey.builder().ephemeralKey(DoordeckSDK.keys.public).build()
            val result: Result<CertificateChain> = client.certificateService().registerEphemeralKey(ephKey).awaitResult()
            when (result) {
                is Result.Ok -> {
                    resolveTile(tileId)
                    DoordeckSDK.certificateChain = result.value
                    EventsManager.sendEvent(EventAction.GET_CERTIFICATE_SUCCESS)
                }
                is Result.Error -> {
                    if (result.response.code() == 423) {
                        EventsManager.sendEvent(EventAction.TWO_FACTOR_AUTH_NEEDED)
                        view?.displayVerificationView()
                    } else {
                        EventsManager.sendEvent(EventAction.INVALID_AUTH_TOKEN)
                        accessDenied()
                    }
                }
                is Result.Exception -> {
                    accessDenied()
                    val errorMsg = result.exception.message
                    if (errorMsg != null && errorMsg.contains("Unable to resolve host"))
                        EventsManager.sendEvent(EventAction.NO_INTERNET, result.exception)
                    else
                        EventsManager.sendEvent(EventAction.SDK_NETWORK_ERROR, result.exception)
                    LOG.e(TAG, "getCertificate:  Something broken : " + result.exception)
                }
            }
        }
    }

    /**
     * Call the server to get the detail of the device given the tileId
     * @param tileId id of the tile scanned
     */
    private fun resolveTile(tileId: String) {
        jobs += GlobalScope.launch(Dispatchers.Main) {
            val result: Result<Device> = client.device().resolveTile(UUID.fromString(tileId)).awaitResult()
            when (result) {
                is Result.Ok -> resolveTileSuccess(result.value)
                is Result.Error -> {
                    accessDenied()
                    EventsManager.sendEvent(EventAction.RESOLVE_TILE_FAILED, result.exception)
                    LOG.e(TAG, "resolveTile err : " + result.exception)
                }
                is Result.Exception -> {
                    EventsManager.sendEvent(EventAction.SDK_NETWORK_ERROR, result.exception)
                    LOG.e(TAG, "resolveTile Something broken : " + result.exception)
                }
            }
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
            unlockDevice(device.deviceId())
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
     */
    private fun unlockDevice(deviceId: UUID) {

        DoordeckSDK.certificateChain?.let { chain ->
            jobs += GlobalScope.launch(Dispatchers.Main) {

                val signedJWT = JWTSignedUtils.getSignedJWT(chain.certificateChain(),
                        DoordeckSDK.keys.private,
                        deviceId,
                        chain.userId()
                )

                val result: Response<Void> = client.device().executeOperation(deviceId, signedJWT).awaitResponse()
                when (result.isSuccessful) {
                    true -> {
                        EventsManager.sendEvent(EventAction.UNLOCK_SUCCESS)
                        view?.unlockSuccess()
                    }
                    false -> {
                        EventsManager.sendEvent(EventAction.CODE_VERIFICATION_FAILED, result.message())
                        Log.e("resolveTileSuccess", "Error result : " + result.message())
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
