package com.doordeck.sdk.common.manager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import com.doordeck.sdk.BuildConfig
import com.doordeck.sdk.common.events.DDEventCallback
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.models.DDEVENT
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.common.utils.DoordeckPreconditions
import com.doordeck.sdk.dto.certificate.CertificateChain
import com.doordeck.sdk.http.DoordeckClient
import com.doordeck.sdk.signer.Ed25519KeyGenerator
import com.doordeck.sdk.ui.nfc.NFCActivity
import com.doordeck.sdk.ui.qrcode.QRcodeActivity
import io.reactivex.Observable
import java.net.URI
import java.security.GeneralSecurityException
import java.security.KeyPair


object DoordeckSDK {

    private const val LOG = "DoordeckSDK"

    // usage of the light theme boolean
    internal var lightTheme: Boolean = false

    // current api key
    internal var apiKey: String? = null

    // HTTP client
    internal lateinit var client: DoordeckClient
    // pub/priv key
    internal lateinit var keys: KeyPair
    // Android keychain
    internal val datastore = Datastore()
    // callbacks for the user
    internal var callback: DDEventCallback? = null
    // certificates associated to the current user
    internal var certificateChain: CertificateChain? = null

    // public //

    /**
     * Initialize the Doordeck SDK and get the Api key from the manifest file.
     * That's the first method to call, preferable in your Application or MainActivity, before using
     * the SDK.
     * @param context current context
     * @return DoordeckSDK the current instance of the SDK
     */
    fun initialize(context: Context): DoordeckSDK {
        if (apiKey == null) {
            setupPrerequise(context)
            apiKey = getApiKeyFromManifest(context)
            DoordeckPreconditions.checkArgument(!TextUtils.isEmpty(apiKey), "API key is not present. Please provide the key in the AndroidManifest.xml")
            createHttpClient()
            getKeys()
        } else
            Log.d(LOG, "Doordeck already initialized")
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
     * Set the light theme for the SDK
     * @return DoordeckSDK the current instance of the SDK
     */
    fun withLightTheme(): DoordeckSDK {
        lightTheme = true
        return this
    }

    /**
     * Set the Dark theme for the SDK
     * @return DoordeckSDK the current instance of the SDK
     */
    fun withDarkTheme(): DoordeckSDK {
        lightTheme = false
        return this
    }

    /**
     * Close the QR Code view, if open
     */
    fun closeQrCodeView() {
        EventsManager.sendEvent(EventAction.CLOSE_QR_CODE_VIEW)
    }

    /**
     * Close the NFC scan view, if open
     */
    fun closeNFCView() {
        EventsManager.sendEvent(EventAction.CLOSE_NFC_VIEW)
    }

    /**
     * Set a callback to listen to the events sent by the SDK
     */
    fun withEventsCallback(callback: DDEventCallback) {
        this.callback = callback
    }

    /**
     * OPen the QR code view (Activity) to scan the tile
     * @param context current context
     */
    fun scanWithQrCode(context: Context) {
        QRcodeActivity.start(context)
    }

    /**
     * Open the NFC view (Activity) to scan the tile
     * @param context current context
     */
    fun scanWithNFC(context: Context) {
        NFCActivity.start(context)
    }

    // private //

    /**
     * Create the network HTTP client
     */

    private fun createHttpClient() {
        this.client = DoordeckClient.Builder()
                .baseUrl(URI.create(BuildConfig.BASE_URL_API))
                .authToken(apiKey)
                .build()
    }

    /**
     * Generate the private/public key and store them in the keychains
     */
    private fun getKeys() {
        val keys = datastore.getKeyPair()
        if (keys == null || keys.private == null || keys.public == null) {
            try {
                this.keys = Ed25519KeyGenerator.generate()
                datastore.saveKeyPair(this.keys)
            } catch (e: GeneralSecurityException) {
                e.printStackTrace()
            }
        } else
            this.keys = keys
    }


    /**
     * setup the SDK's prerequise
     */
    private fun setupPrerequise(context: Context) {
        DoordeckPreconditions.checkNotNull(context, "Context can't be null")
        DoordeckPreconditions.checkTLS(context)
    }

    /**
     * Get the api key from the Manifest. the key should be : doordeck.API_KEY
     * @param context current context
     * @return the API key, if found, null otherwise
     */
    private fun getApiKeyFromManifest(context: Context): String? {
        val appInfo: ApplicationInfo?
        try {
            appInfo = context.applicationContext.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            throw IllegalStateException("Could not find application package name")
        }

        DoordeckPreconditions.checkArgument(appInfo != null,
                "Application info not found in the manifest. Please check your AndroidManifest.xml")
        DoordeckPreconditions.checkArgument(appInfo!!.metaData != null,
                "Meta data not found in the manifest. Please provide meta-data in the AndroidManifest.xml")

        return appInfo.metaData.getString("doordeck.API_KEY")
    }


}