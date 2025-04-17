package com.doordeck.sdk.common.manager

import android.content.Context
import com.doordeck.multiplatform.sdk.ApplicationContext
import com.doordeck.multiplatform.sdk.KDoordeckFactory
import com.doordeck.multiplatform.sdk.config.SdkConfig
import com.doordeck.multiplatform.sdk.model.responses.LockResponse
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.models.DoordeckEvent
import com.doordeck.sdk.common.utils.isUUID
import com.doordeck.sdk.ui.nfc.NFCActivity
import com.doordeck.sdk.ui.qrcode.QRcodeActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity.Companion.COMING_FROM_DIRECT_UNLOCK
import com.doordeck.sdk.ui.verify.VerifyDeviceActivity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.CompletableFuture


@Suppress("OVERLOADS_WITHOUT_DEFAULT_ARGUMENTS")
object Doordeck {

    // usage of the light theme boolean
    internal var darkMode: Boolean = true

    // headless sdk instance
    private var doordeck: com.doordeck.multiplatform.sdk.Doordeck? = null // Not initialized yet
    private val requireDoordeck: com.doordeck.multiplatform.sdk.Doordeck
        get() = doordeck!!

    private var objectToUnlock: ObjectToUnlock? = null

    // public //

    /**
     * Initialize the Doordeck SDK with your valid auth token
     * This is the first method to call from the your Android Application class. The reason for this being in the Application class is
     * for the SDK to be able to initialize and unlock from NFC touch, even when the parent app is not running in the background yet.
     *
     * @param ctx Your application context! Warning, providing non-application context might break the app or cause memory leaks.
     * @param darkMode (Optional) set dark or light theme of the sdk.
     * @return Doordeck the current instance of the SDK
     */
    @JvmOverloads
    fun initialize(
        ctx: Context,
        darkMode: Boolean = false,
    ): Doordeck {
        this.darkMode = darkMode
        doordeck = KDoordeckFactory.initialize(
            SdkConfig.Builder()
                .setApplicationContext(ApplicationContext.apply { set(ctx) })
                .build()
        )
        return this
    }

    /**
     * Update your authToken
     * Call this method after logging in to update the token.
     * @param authToken new valid auth token
     */
    @JvmOverloads
    fun setToken(authToken: String) {
        if (authToken.isBlank()) throw IllegalArgumentException("Token needs to be provided")

        setKeyPairIfNeeded()
        requireDoordeck.contextManager().setCloudAuthToken(authToken)
    }

    /**
     * Update the theme
     * @param darkMode set dark or light theme
     */
    fun setDarkMode(darkMode: Boolean) {
        this.darkMode = darkMode
    }

    /**
     * Observable to subscribe to, to be able to listen to the events sent by the SDK
     * @return Observable of events
     */
    fun eventsFlow(): Flow<DoordeckEvent> {
        return EventsManager.eventsFlow()
    }

    /**
     * Unlock method for unlocking via button unlock
     *
     * @param ctx current Context
     * @param device a valid device.
     *
     */
    @JvmOverloads
    fun unlock(ctx: Context, device: LockResponse) {
        this.objectToUnlock = DeviceToUnlock(device)
        showUnlock(ctx, ScanType.UNLOCK)
    }

    /**
     * Unlock method for unlocking via UUID
     *
     * @param context current Context
     * @param tileId: Tile UUID is UUID for a ile from a deeplink or QR or background NFC
     *
     */
    @JvmOverloads
    fun unlockTileID(context: Context, tileId: String): CompletableFuture<Void> {
        if (isUUID(tileId)) {
            this.objectToUnlock = TileIdToUnlock(UUID.fromString(tileId))
            return showUnlock(context, ScanType.UNLOCK)
        } else {
            throw IllegalStateException("No valid UUID")
        }
    }

    /**
     * Show the unlock screen given the Scan type given in parameter
     *
     * @param context current context
     * @param type type of scan to use (NFC or QR) , NFC by default if not provided, optional
     */
    @JvmOverloads
    fun showUnlock(context: Context, type: ScanType = ScanType.NFC): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            when (type) {
                ScanType.QR -> QRcodeActivity.start(context)
                ScanType.NFC -> NFCActivity.start(context)
                ScanType.UNLOCK -> when (val objectToUnlock = this.objectToUnlock) {
                    is DeviceToUnlock -> UnlockActivity.start(
                        context = context,
                        device = objectToUnlock.device,
                        comingFrom = COMING_FROM_DIRECT_UNLOCK,
                    )

                    is TileIdToUnlock -> UnlockActivity.start(
                        context = context,
                        tileId = objectToUnlock.tileID.toString(),
                        comingFrom = COMING_FROM_DIRECT_UNLOCK,
                    )

                    null -> {
                        // NO-OP
                    }
                }
            }

            return@runAsync
        }
    }

    val authStatus: AuthStatus
        get() = requireDoordeck.authStatus

    fun getHeadlessInstance(context: Context): com.doordeck.multiplatform.sdk.Doordeck {
        if (doordeck == null) {
            doordeck = KDoordeckFactory.initialize(
                SdkConfig.Builder()
                    .setApplicationContext(ApplicationContext.apply { set(context) })
                    .build()
            )
        }

        return requireDoordeck
    }

    /**
     * Cleanup the data internally
     * Call when you log out a user.
     */
    fun logout(): CompletableFuture<Unit> {
        return requireDoordeck.account().logoutAsync()
    }

    /**
     * Shows the verification screen
     */
    fun showVerificationScreen(context: Context) {
        VerifyDeviceActivity.start(context)
    }

    private fun setKeyPairIfNeeded() {
        if (!requireDoordeck.contextManager().isKeyPairValid()) {
            val newKeyPair = requireDoordeck.crypto().generateKeyPair()
            requireDoordeck.contextManager().setKeyPair(
                publicKey = newKeyPair.public,
                privateKey = newKeyPair.private,
            )
        }
    }
}