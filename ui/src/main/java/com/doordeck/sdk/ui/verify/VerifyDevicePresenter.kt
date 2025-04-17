package com.doordeck.sdk.ui.verify

import android.content.Context
import com.doordeck.multiplatform.sdk.model.responses.UserDetailsResponse
import com.doordeck.sdk.common.manager.Doordeck
import java.util.concurrent.CompletableFuture

/**
 * Logic for the verify device view
 */
internal class VerifyDevicePresenter {

    private lateinit var view: VerifyDeviceView

    fun onStart(view: VerifyDeviceView) {
        this.view = view
    }

    /**
     * callback when the user click on the button "re-send code"
     */
    fun onSendCode(): CompletableFuture<UserDetailsResponse> {
        return Doordeck.getHeadlessInstance(view as Context).helper().assistedRegisterEphemeralKeyAsync()
            .thenCompose {
                return@thenCompose Doordeck.getHeadlessInstance(view as Context).account().getUserDetailsAsync()
            }
    }

    /**
     * callback to verify the code entered
     * @param code  code entered by the user, to validate
     */
    fun verifyCode(code: String) {
        Doordeck.getHeadlessInstance(view as Context).account().verifyEphemeralKeyRegistrationAsync(code)
            .thenAccept {
                view.succeed()
            }
            .exceptionally {
                view.fail()
                return@exceptionally null
            }
    }
}
