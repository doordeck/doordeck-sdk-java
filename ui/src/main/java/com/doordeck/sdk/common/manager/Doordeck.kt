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

object Doordeck {

    internal var darkMode: Boolean = true
    private var doordeck: com.doordeck.multiplatform.sdk.Doordeck? = null

    private val requireDoordeck: com.doordeck.multiplatform.sdk.Doordeck
        get() = doordeck!!

    private var objectToUnlock: ObjectToUnlock? = null

    /**
     * Initializes the Doordeck SDK.
     *
     * You must call this before using any other SDK methods.
     * It can be called at any point in your app lifecycle, and with any valid context.
     *
     * @param context Any valid Android context (application or activity).
     * @param darkMode Optional: Enables dark mode for SDK UI. Default is false.
     * @return The initialized Doordeck instance.
     */
    @JvmOverloads
    fun initialize(
        context: Context,
        darkMode: Boolean = false,
    ): Doordeck {
        this.darkMode = darkMode
        doordeck = KDoordeckFactory.initialize(
            SdkConfig.Builder()
                .setApplicationContext(ApplicationContext.apply { set(context) })
                .build()
        )
        return this
    }

    /**
     * Updates the authentication token for the SDK.
     *
     * Must be called after login with a valid Doordeck JWT token.
     *
     * @param authToken The Doordeck JWT auth token.
     */
    fun setToken(authToken: String) {
        if (authToken.isBlank()) throw IllegalArgumentException("Token needs to be provided")

        setKeyPairIfNeeded()
        requireDoordeck.contextManager().setCloudAuthToken(authToken)
    }

    /**
     * Updates the UI theme for the SDK.
     *
     * @param darkMode Whether to enable dark mode.
     */
    fun setDarkMode(darkMode: Boolean) {
        this.darkMode = darkMode
    }

    /**
     * Flow for observing SDK events.
     *
     * @return A Flow of DoordeckEvent values.
     */
    fun eventsFlow(): Flow<DoordeckEvent> {
        return EventsManager.eventsFlow()
    }

    /**
     * Triggers the unlock flow for a given device.
     *
     * @param ctx Context for launching the unlock screen.
     * @param device The LockResponse representing the device to unlock.
     * @return A CompletableFuture that completes when the UI is shown.
     */
    fun unlock(ctx: Context, device: LockResponse): CompletableFuture<Void> {
        this.objectToUnlock = DeviceToUnlock(device)
        return showUnlock(ctx, ScanType.UNLOCK)
    }

    /**
     * Triggers the unlock flow using a tile UUID (e.g. from QR or NFC).
     *
     * @param context Valid context to launch the unlock screen.
     * @param tileId A valid tile UUID string.
     * @return A CompletableFuture that completes when the UI is shown.
     */
    fun unlockTileID(context: Context, tileId: String): CompletableFuture<Void> {
        if (isUUID(tileId)) {
            this.objectToUnlock = TileIdToUnlock(UUID.fromString(tileId))
            return showUnlock(context, ScanType.UNLOCK)
        } else {
            throw IllegalStateException("No valid UUID")
        }
    }

    /**
     * Shows the unlock screen using the specified scan type.
     *
     * @param context Context for launching the screen.
     * @param type Type of unlock scan (NFC, QR, or UNLOCK).
     * @return A CompletableFuture that completes after navigation is triggered.
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
                        // No-op if nothing to unlock
                    }
                }
            }

            return@runAsync
        }
    }

    /**
     * Returns the current authentication status of the SDK.
     */
    val authStatus: AuthStatus
        get() = requireDoordeck.authStatus

    /**
     * Retrieves the headless instance of the SDK.
     *
     * Lazily initializes if not yet done.
     *
     * @param context A valid context (preferably application context).
     * @return The internal Doordeck instance.
     */
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
     * Logs out the current user and clears all local SDK data.
     *
     * @return A CompletableFuture that resolves once logout completes.
     */
    fun logout(): CompletableFuture<Unit> {
        return requireDoordeck.account().logoutAsync()
    }

    /**
     * Shows the device verification screen.
     *
     * Call this to allow the user to verify their device if needed.
     *
     * @param context A valid context for navigation.
     */
    fun showVerificationScreen(context: Context) {
        VerifyDeviceActivity.start(context)
    }

    /**
     * Helper.
     * Generates and sets a key pair if none exists.
     */
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