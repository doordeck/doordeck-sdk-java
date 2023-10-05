package com.doordeck.sdk.common.manager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.events.IEventCallback
import com.doordeck.sdk.common.events.UnlockCallback
import com.doordeck.sdk.common.models.DDEVENT
import com.doordeck.sdk.common.models.PartialDevice
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.common.models.JWTHeader
import com.doordeck.sdk.common.utils.JWTContentUtils
import com.doordeck.sdk.common.utils.LOG
import com.doordeck.sdk.dto.certificate.CertificateChain
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.dto.operation.Operation
import com.doordeck.sdk.http.DoordeckClient
import com.doordeck.sdk.signer.Ed25519KeyGenerator
import com.doordeck.sdk.signer.util.JWTUtils
import com.doordeck.sdk.ui.nfc.NFCActivity
import com.doordeck.sdk.ui.qrcode.QRcodeActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity.Companion.COMING_FROM_DIRECT_UNLOCK
import com.doordeck.sdk.ui.verify.VerifyDeviceActivity
import com.github.doordeck.ui.BuildConfig
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
    // unlock callback
    internal var unlockCallback: UnlockCallback? = null
    // get
    internal fun getKeys(): KeyPair {
        return keys!!
    }
    // device to unlock
    private var deviceToUnlock: Device? = null

    // Shared Preferences
    @SuppressLint("StaticFieldLeak")
    internal var sharedPreference: SharedPreference? = null
    // Observable to catch async certificate loading
    internal var certificateLoaded: Boolean by observable(false) { _, oldValue, newValue ->
        onCertLoaded?.invoke(oldValue, newValue)
    }
    // Check if certificates are loaded
    var onCertLoaded : ((Boolean, Boolean) -> Unit)? = null

    // Sdk Mode
    private var sdkMode: Boolean = false



    // public //

    /**
     * Initialize the Doordeck SDK with your valid auth token
     * This is the first method to call from the your Android Application class. The reason for this being in the Application class is
     * for the SDK to be able to initialize and unlock from NFC touch, even when the parent app is not running in the background yet.
     *
     * @param ctx Your application context! Warning, providing non-application context might break the app or cause memory leaks.
     * @param authToken (Nullable) A valid auth token. Make sure you refresh the auth token if needed before initializing the SDK.
     * If you don't have an auth token yet because the user is logged out, initiate the sdk with authToken = null and set the auth token after logging in with updateToken method.
     * @param darkMode (Optional) set dark or light theme of the sdk.
     * @param unlockCallback provides a callback mainly for Auth purposes
     * @return Doordeck the current instance of the SDK
     */
    @JvmOverloads
    fun initialize(
            ctx: Context,
            authToken: String? = null,
            darkMode: Boolean = false,
            unlockCallback: UnlockCallback? = null
    ): Doordeck {
        if (authToken != null) {
            if (this.apiKey == null) {
                val jwtToken = JWTContentUtils.getContentHeaderFromJson(authToken) ?: throw IllegalArgumentException("Api key is invalid")
                if (!isValidityApiKey(jwtToken)) throw IllegalArgumentException("Api key has expired")
                this.jwtToken = jwtToken
                this.apiKey = authToken
                this.darkMode = darkMode
                this.sharedPreference = SharedPreference(ctx)
                createHttpClient()
                generateKeys(ctx)
                this.storeTheme(darkMode)
                if (getStoredAuthToken(ctx) != authToken) {
                    storeToken(ctx, authToken)
                    keys?.public?.let { CertificateManager.getCertificatesAsync(it, unlockCallback) }
                } else {
                    if (certificateChain == null) {
                        certificateChain = getStoredCertificateChain()
                        if (certificateChain == null) {
                            keys?.public?.let { CertificateManager.getCertificatesAsync(it, unlockCallback) }
                            val lastStatus = getLastStatus()
                            if (lastStatus != null) Doordeck.status = lastStatus
                        } else {
                            if (checkIfValidCertificate(certificateChain!!)) {
                                status = AuthStatus.AUTHORIZED
                                certificateLoaded = true
                            } else {
                                keys?.public?.let { CertificateManager.getCertificatesAsync(it, unlockCallback) }
                            }
                        }
                    } else {
                        if (checkIfValidCertificate(certificateChain!!)) {
                            status = AuthStatus.AUTHORIZED
                            certificateLoaded = true
                        } else {
                            keys?.public?.let { CertificateManager.getCertificatesAsync(it, unlockCallback) }
                        }
                    }
                }
            } else Log.d(LOG_SDK, "Doordeck already initialized")
        } else {
            this.darkMode = darkMode
            this.sharedPreference = SharedPreference(ctx)
            createHttpClient()
            this.storeTheme(darkMode)
        }
        return this
    }

    private fun checkIfValidCertificate(certificateChain: CertificateChain): Boolean {
        return certificateChain.isValid() && certificateChain.userId().toString() == this.jwtToken!!.sub()
    }

    /**
     * Update your authToken
     * Call this method after logging in to update the token.
     * @param authToken new valid auth token
     * @param unlockCallback provides a callback mainly for Auth purposes
     */
    @JvmOverloads
    fun updateToken(authToken: String, ctx: Context, unlockCallback: UnlockCallback? = null) {
        if (sharedPreference == null) throw IllegalArgumentException("Doordeck not initiated. Make sure to call initialize first.")
        if (authToken.isBlank()) throw IllegalArgumentException("Token needs to be provided")
        val jwtToken = JWTContentUtils.getContentHeaderFromJson(authToken) ?: throw IllegalArgumentException("Api key is invalid")
        if (!isValidityApiKey(jwtToken)) throw IllegalArgumentException("Api key has expired")
        this.jwtToken = jwtToken
        this.apiKey = authToken
        this.darkMode = darkMode
        generateKeys(ctx)
        storeToken(ctx, authToken)
        storeTheme(darkMode)
        createHttpClient()
        keys?.public?.let { CertificateManager.getCertificatesAsync(it, unlockCallback) }
    }

    /**
     * Update the theme
     * @param darkMode set dark or light theme
     */
    fun setDarkMode(darkMode: Boolean) {
        this.darkMode = darkMode
        this.storeTheme(darkMode)
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


    /**
     * Unlock method for unlocking via button unlock
     *
     * @param ctx current Context
     * @param device a valid device.
     * @param callback (optional) callback function for catching async response after unlock.
     *
     */
    @JvmOverloads
    fun unlock(ctx: Context, device: Device, callback: UnlockCallback? = null){
        this.deviceToUnlock = device
        showUnlock(ctx, ScanType.UNLOCK, callback)
    }

    /**
     * Unlock method for unlocking via UUID
     *
     * @param ctx current Context
     * @param tileID: Tile UUID is UUID for a ile from a deeplink or QR or background NFC
     * @param callback (optional) callback function for catching async response after unlock.
     *
     */
    @JvmOverloads
    fun unlockTileID(ctx: Context, tileID: String, callback: UnlockCallback? = null){
        this.deviceToUnlock = PartialDevice(tileID)
        showUnlock(ctx, ScanType.UNLOCK, callback)
    }

    /**
     * Show the unlock screen given the Scan type given in parameter
     *
     * @param context current context
     * @param type type of scan to use (NFC or QR) , NFC by default if not provided, optional
     * @param callback callback of the method, optional
     */
    @JvmOverloads
    fun showUnlock(context: Context, type: ScanType = ScanType.NFC, callback: UnlockCallback? = null) {

        if (status == AuthStatus.UNAUTHORIZED) {
            callback?.notAuthenticated()
            return
        }

        if (status == AuthStatus.TWO_FACTOR_AUTH_NEEDED) {
            callback?.verificationNeeded()
            return
        }

        jwtToken?.let { header ->
            if (isValidityApiKey(header) && apiKey != null) {
                if (!sdkMode) {
                    when (type) {
                        ScanType.QR -> QRcodeActivity.start(context)
                        ScanType.NFC -> NFCActivity.start(context)
                        ScanType.UNLOCK -> when(deviceToUnlock) {
                            is PartialDevice -> UnlockActivity.start(
                                context = context,
                                id = deviceToUnlock!!.deviceId().toString(),
                                comingFrom = COMING_FROM_DIRECT_UNLOCK,
                            )
                            else -> UnlockActivity.start(
                                context = context,
                                device = deviceToUnlock!!,
                                comingFrom = COMING_FROM_DIRECT_UNLOCK,
                            )
                        }
                    }
                }
                this.unlockCallback = callback
            } else
                callback?.invalidAuthToken()
        }
    }

    /**
     * getSigned JWT
     *
     * @param deviceId
     * @param operation operation of the signed key
     */
    fun getSignedJWT(deviceId: UUID, operation: Operation): String {
        Doordeck.certificateChain?.let { chain ->
            return JWTUtils.getSignedJWT(chain.certificateChain(),
                    Doordeck.getKeys().private,
                    deviceId,
                    chain.userId(),
                    operation
            )
        } ?: return ""
    }


    /**
     * Cleanup the data internally
     * Call when you log out a user.
     */
    fun logout(context: Context) {
        this.apiKey = null
        this.certificateChain = null
        this.datastore.clean(context)
        this.keys = null
    }

    /**
     * Shows the verification screen
     */
    fun showVerificationScreen(context: Context) {
        VerifyDeviceActivity.start(context)
    }


    // private //


    internal fun hasUserLoggedIn (ctx: Context): Boolean {
        if (this.apiKey != null) return true
        else {
            val token = getStoredAuthToken(ctx)
            if (token !== null) {
                try {
                    initialize(ctx, token, getSavedTheme())
                    return true
                } catch (e: IllegalArgumentException) {
                    return false
                }
            } else return false
        }
    }

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
        if (jwtToken.exp() * 1000L > currentDate)
            return true
        return false
    }


    /**
     * Generate the private/public key and store them in the keychains
     */
    private fun generateKeys(context: Context) {
        val keys = datastore.getKeyPair(context)
        if (keys == null || keys.private == null || keys.public == null) {
            try {
                val keyPair = Ed25519KeyGenerator.generate()
                datastore.saveKeyPair(context, keyPair)
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
    internal fun storeToken(context: Context, authToken: String) {
        datastore.saveAuthToken(context, authToken)
    }

    /**
     * get stored certificates from keychains
     */
    internal fun getStoredAuthToken(context: Context): String? {
        return datastore.getAuthToken(context)
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

    /**
     * Store certificates them in the keychains
     */
    internal fun storeTheme(darkMode: Boolean) {
        datastore.saveTheme(darkMode)
    }

    /**
     * get stored certificates from keychains
     */
    internal fun getSavedTheme(): Boolean {
        val darkMode = datastore.getSavedTheme()
        if (darkMode != null) return darkMode
        else return false
    }


}