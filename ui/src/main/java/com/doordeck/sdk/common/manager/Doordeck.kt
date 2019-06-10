package com.doordeck.sdk.common.manager

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.doordeck.sdk.BuildConfig
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.events.IEventCallback
import com.doordeck.sdk.common.events.UnlockCallback
import com.doordeck.sdk.common.models.DDEVENT
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.common.models.JWTHeader
import com.doordeck.sdk.common.utils.JWTContentUtils
import com.doordeck.sdk.common.utils.LOG
import com.doordeck.sdk.dto.certificate.CertificateChain
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.http.DoordeckClient
import com.doordeck.sdk.signer.Ed25519KeyGenerator
import com.doordeck.sdk.ui.nfc.NFCActivity
import com.doordeck.sdk.ui.qrcode.QRcodeActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.google.common.base.Preconditions
import io.reactivex.Observable
import java.net.URI
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.util.*
import kotlin.properties.Delegates.observable


object Doordeck {

    private const val USER_AGENT_PREFIX = "Doordeck SDK - "

    private const val LOG_SDK = "Doordeck"

    // usage of the light theme boolean
    internal var darkMode: Boolean = true

    // current api key
    private var apiKey: String? = null
    internal var jwtToken: JWTHeader? = null

    internal var status: AuthStatus = AuthStatus.UNAUTHORIZED


    // HTTP client
    internal var client: DoordeckClient? = null
    // pub/priv key
    private var keys: KeyPair? = null
    // Android keychain
    private val datastore = Datastore()
    // callbacks for the user
    internal var callback: IEventCallback? = null
    // certificates associated to the current user
    internal var certificateChain: CertificateChain? = null

    internal var unlockCallback: UnlockCallback? = null

    internal fun getKeys(): KeyPair {
        return keys!!
    }

    private var deviceToUnlock: Device? = null

    internal var sharedPreference: SharedPreference? = null

    internal var certificateLoaded: Boolean by observable(false) { _, oldValue, newValue ->
        onCertLoaded?.invoke(oldValue, newValue)
    }

    var onCertLoaded : ((Boolean, Boolean) -> Unit)? = null



    // public //

    /**
     * Initialize the Doordeck SDK and get the Api key from the manifest file.
     * That's the first method to call in your Application, before using
     * the SDK.
     * @return Doordeck the current instance of the SDK
     */
    fun initialize(ctx: Context, darkMode: Boolean = false): Doordeck {
        if (this.sharedPreference == null) {
            this.sharedPreference = SharedPreference(ctx)
        }
        if (this.apiKey == null) {
            this.apiKey = getStoredAuthToken()
            if (this.apiKey != null) {
                val jwtToken = this.apiKey?.let { JWTContentUtils.getContentHeaderFromJson(it) }
                Preconditions.checkNotNull(jwtToken!!, "Api key is invalid")
                Preconditions.checkArgument(isValidityApiKey(jwtToken), "Api key has expired")
                this.jwtToken = jwtToken
                createHttpClient()
                generateKeys()
                if (certificateChain == null) {
                    certificateChain = getStoredCertificateChain()
                    if (certificateChain == null) {
                        var lastStatus = getLastStatus()
                        if (lastStatus != null) Doordeck.status = lastStatus
                    }
                }
            }
        }
        return this
    }

    /**
     * Initialize the Doordeck SDK and get the Api key from the manifest file.
     * That's the first method to call, preferable in your Application or MainActivity, before using
     * the SDK.
     * @return Doordeck the current instance of the SDK
     */
    fun initialize(ctx: Context, authToken: String, darkMode: Boolean = false): Doordeck {
        Preconditions.checkArgument(!TextUtils.isEmpty(authToken), "authToken needs to be provided")
        if (this.apiKey == null) {
            val mRequestStartTime = System.currentTimeMillis()
            val jwtToken = JWTContentUtils.getContentHeaderFromJson(authToken)
            Preconditions.checkNotNull(jwtToken!!, "Api key is invalid")
            Preconditions.checkArgument(isValidityApiKey(jwtToken), "Api key has expired")
            var totalRequestTime = System.currentTimeMillis() - mRequestStartTime
            Log.v("jwtTokenTime = ", totalRequestTime.toString())
            this.jwtToken = jwtToken
            this.apiKey = authToken
            this.darkMode = darkMode
            this.sharedPreference = SharedPreference(ctx)
            createHttpClient()
            generateKeys()
            totalRequestTime = System.currentTimeMillis() - mRequestStartTime
            Log.v("generateKeysTime = ", totalRequestTime.toString())
            if(getStoredAuthToken() != authToken) {
                storeToken(authToken)
                keys?.public?.let { CertificateManager.getCertificatesAsync(it) }
            } else {
                if (certificateChain == null) {
                    certificateChain = getStoredCertificateChain()
                    totalRequestTime = System.currentTimeMillis() - mRequestStartTime
                    Log.v("getCertificateChain = ", totalRequestTime.toString())
                    if (certificateChain == null) {
                        keys?.public?.let { CertificateManager.getCertificatesAsync(it) }
                        var lastStatus = getLastStatus()
                        if (lastStatus != null) Doordeck.status = lastStatus
                    } else {
                        status = AuthStatus.AUTHORIZED
                        certificateLoaded = true
                    }
                }
            }
        } else
            Log.d(LOG_SDK, "Doordeck already initialized")
        return this
    }

    // public //

    /**
     * Initialize the Doordeck SDK and get the Api key from the manifest file.
     * That's the first method to call, preferable in your Application or MainActivity, before using
     * the SDK.
     * @return Doordeck the current instance of the SDK
     */
    fun updateToken(authToken: String): Doordeck {
        Preconditions.checkArgument(!TextUtils.isEmpty(authToken), "Token needs to be provided")
        val jwtToken = JWTContentUtils.getContentHeaderFromJson(authToken)
        Preconditions.checkNotNull(jwtToken!!, "Api key is invalid")
        Preconditions.checkArgument(isValidityApiKey(jwtToken), "Api key has expired")
        this.jwtToken = jwtToken
        this.apiKey = authToken
        this.darkMode = darkMode
        generateKeys()
        storeToken(authToken)
        keys?.public?.let { CertificateManager.getCertificatesAsync(it) }
        return this
    }


    /**
     * Observable to subscribe to, to be able to listen to the events sent by the SDK
     * @return Observable of events
     */
    fun eventsObservable(): Observable<DDEVENT> {
        return EventsManager.eventsObservable()
    }

    /**
     * Set a callback to listen to the events sent by the SDK
     */
    fun withEventsCallback(callback: IEventCallback) {
        this.callback = callback
    }



    fun unlock(ctx: Context, device: Device, callback: UnlockCallback? = null){
        this.deviceToUnlock = device
        showUnlock(ctx, ScanType.UNLOCK, callback)
    }

    /**
     * Show the unlock screen given the Scan type given in parameter
     *
     * @param context current context
     * @param type type of scan to use (NFC or QR) , NFC by default if not provided, optional
     * @param callback callback of the method, optional
     */
    fun showUnlock(context: Context, type: ScanType = ScanType.NFC, callback: UnlockCallback? = null) {

        if (status == AuthStatus.UNAUTHORIZED) {
            callback?.notAuthenticated()
            return
        }
        jwtToken?.let { header ->
            if (isValidityApiKey(header) && apiKey != null) {

                when (type) {
                    ScanType.QR -> QRcodeActivity.start(context)
                    ScanType.NFC -> NFCActivity.start(context)
                    ScanType.UNLOCK -> UnlockActivity.start(context, deviceToUnlock!!)
                }
                this.unlockCallback = callback
            } else
                callback?.invalidAuthToken()
        }
    }


    /**
     * Cleanup the data internally
     */
    fun logout() {
        apiKey = null
        datastore.clean()
        keys = null
        generateKeys()
    }


    // private //

    /**
     * Create the network HTTP client
     */

    internal fun createHttpClient() {
        this.client = DoordeckClient.Builder()
                .baseUrl(URI.create(BuildConfig.BASE_URL_API))
                .userAgent(USER_AGENT_PREFIX + BuildConfig.VERSION_NAME)
                .authToken(apiKey)
                .build()
    }


    /**
     * Verify if the expiry date provided inside the JWT token is valid
     * @return true if valid, false otherwise
     */
    private fun isValidityApiKey(jwtToken: JWTHeader): Boolean {
        val currentDate = Date().time
        if (jwtToken.exp * 1000L > currentDate)
            return true
        return false
    }


    /**
     * Generate the private/public key and store them in the keychains
     */
    private fun generateKeys() {
        val keys = datastore.getKeyPair()
        if (keys == null || keys.private == null || keys.public == null) {
            try {
                val keyPair = Ed25519KeyGenerator.generate()
                datastore.saveKeyPair(keyPair)
                this.keys = keyPair
            } catch (e: GeneralSecurityException) {
                LOG.e(LOG_SDK, e.localizedMessage)
                EventsManager.sendEvent(EventAction.SDK_ERROR, e)

            }
        } else
            this.keys = keys
    }

    /**
     * Store certificates them in the keychains
     */
    internal fun storeCertificates(certificateChain: CertificateChain) {
        datastore.saveCertificates(certificateChain)
    }

    /**
     * get stored certificates from keychains
     */
    internal fun getStoredCertificateChain(): CertificateChain? {
        return datastore.getSavedCertificates()
    }

    /**
     * Store certificates them in the keychains
     */
    internal fun storeToken(authToken: String) {
        datastore.saveAuthToken(authToken)
    }

    /**
     * get stored certificates from keychains
     */
    internal fun getStoredAuthToken(): String? {
        return datastore.getAuthToken()
    }

    /**
     * Store certificates them in the keychains
     */
    internal fun storeLaststatus(status: AuthStatus) {
        datastore.saveStatus(status)
    }

    /**
     * get stored certificates from keychains
     */
    internal fun getLastStatus(): AuthStatus? {
        return datastore.getStoredStatus()
    }


}