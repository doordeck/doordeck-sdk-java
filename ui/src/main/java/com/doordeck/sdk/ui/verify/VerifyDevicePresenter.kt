package com.doordeck.sdk.ui.verify

import com.doordeck.multiplatform.sdk.model.responses.UserDetailsResponse
import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.utils.LOG
import java.util.concurrent.CompletableFuture

/**
 * Logic for the verify device view
 */
internal class VerifyDevicePresenter {

    private val TAG = VerifyDevicePresenter::class.java.canonicalName?.toString() ?: ""
    private var view: VerifyDeviceView? = null

    fun onStart(view: VerifyDeviceView) {
        this.view = view
    }

    /**
     * clean memory
     */
    fun onStop() {
        this.view = null
    }

    /**
     * callback when the user click on the button "re-send code"
     */
    fun onSendCode(): CompletableFuture<UserDetailsResponse> {
        return Doordeck.getHeadlessInstance().helper().assistedRegisterEphemeralKeyAsync()
            .thenCompose {
                return@thenCompose Doordeck.getHeadlessInstance().account().getUserDetailsAsync()
            }
    }

    /**
     * callback to verify the code entered
     * @param code  code entered by the user, to validate
     */
    fun verifyCode(code: String) {
        Doordeck.getHeadlessInstance().account().verifyEphemeralKeyRegistrationAsync(code)
            .thenAccept {
                view?.succeed()
            }
            .exceptionally {
                LOG.e(TAG, "verifyCode error : $it")
                view?.fail()
                return@exceptionally null
            }
    }
}
