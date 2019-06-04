package com.doordeck.sdk.ui.unlock

import android.app.Activity
import android.util.Log
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.manager.AuthStatus
import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.common.services.LocationService
import com.doordeck.sdk.common.utils.LOG
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.http.DoordeckClient
import com.doordeck.sdk.signer.util.JWTUtils
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
    private var client: DoordeckClient? = null
    private var view: UnlockView? = null

    private var mRequestStartTime: Long = 0

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
        if (Doordeck.certificateChain == null && Doordeck.status != AuthStatus.TWO_FACTOR_AUTH_NEEDED)
        {
            Doordeck.onCertLoaded = { oldValue, newValue ->
                if(newValue == true) initUnlock(tileId)
            }
        } else {
            initUnlock(tileId)
        }

    }

    private fun initUnlock(tileId: String?) {

        if (Doordeck.client != null) {
            this.client = Doordeck.client
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
//        if (certif == null) {
//           certif = Doordeck.getStoredCertificateChain()
//        }
        certif?.let { resolveTile(tileId) }
    }



    /**
     * Call the server to get the detail of the device given the tileId
     * @param tileId id of the tile scanned
     */
    private fun resolveTile(tileId: String) {
        Log.v("TILEREQUESTINIT = ", "now")
        mRequestStartTime = System.currentTimeMillis()
        jobs += GlobalScope.launch(Dispatchers.Main) {
            totalRequestTime = System.currentTimeMillis() - mRequestStartTime
            Log.v("TILEREQUESTSTART = ", totalRequestTime.toString())
            val result: Result<Device> = client!!.device().resolveTile(UUID.fromString(tileId)).awaitResult()
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

    private var totalRequestTime: Long = 0

    private fun resolveTileSuccess(device: Device) {
        totalRequestTime = System.currentTimeMillis() - mRequestStartTime
        Log.v("TILEREQUEST = ", totalRequestTime.toString())
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
        mRequestStartTime = System.currentTimeMillis()
        Doordeck.certificateChain?.let { chain ->
            jobs += GlobalScope.launch(Dispatchers.Main) {

                val signedJWT = JWTUtils.getSignedJWT(chain.certificateChain(),
                        Doordeck.getKeys().private,
                        deviceId,
                        chain.userId()
                )

                val result: Response<Void> = client!!.device().executeOperation(deviceId, signedJWT).awaitResponse()
                when (result.isSuccessful) {
                    true -> {
                        totalRequestTime = System.currentTimeMillis() - mRequestStartTime
                        Log.v("UNLOCKTIME = ", totalRequestTime.toString())
                        EventsManager.sendEvent(EventAction.UNLOCK_SUCCESS)
                        view?.unlockSuccess()
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
