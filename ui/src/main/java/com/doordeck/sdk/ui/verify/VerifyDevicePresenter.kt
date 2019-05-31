package com.doordeck.sdk.ui.verify

import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.manager.AuthStatus
import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.common.utils.LOG
import com.doordeck.sdk.dto.certificate.CertificateChain
import com.doordeck.sdk.dto.certificate.ImmutableRegisterEphemeralKey
import com.doordeck.sdk.dto.certificate.ImmutableVerificationRequest
import com.doordeck.sdk.dto.certificate.VerificationMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.gildor.coroutines.retrofit.Result
import ru.gildor.coroutines.retrofit.awaitResponse
import ru.gildor.coroutines.retrofit.awaitResult

/**
 * Logic for the verify device view
 */
internal class VerifyDevicePresenter {

    private val TAG = VerifyDevicePresenter::class.java.canonicalName
    private var view: VerifyDeviceView? = null
    private var jobs: List<Job> = emptyList()
    private val client = Doordeck.client
    private var method = VerificationMethod.TELEPHONE

    fun onStart(view: VerifyDeviceView) {
        this.view = view
        checkMethodUsed(view)
    }

    // given the JWtToken of the user, check with method to contact the user
    private fun checkMethodUsed(view: VerifyDeviceView) {
        Doordeck.jwtToken?.let { header ->
            when {
                header.phone_number != null -> {
                    method = VerificationMethod.TELEPHONE
                    view.setPhoneNumber(header.phone_number)
                }
                header.email != null -> {
                    method = VerificationMethod.EMAIL
                    view.setEmail(header.email)
                }
                else -> view.noMethodDefined()
            }
        }
    }


    /**
     * clean memory
     */
    fun onStop() {
        this.view = null
        jobs.forEach { it.cancel() }
    }

    /**
     * callback when the user click on the button "re-send code"
     */
    fun onSendCode() {
        jobs += GlobalScope.launch(Dispatchers.Main) {

            val ephKey = ImmutableRegisterEphemeralKey.builder().ephemeralKey(Doordeck.getKeys().public).build()
            val result: Response<Void> = client!!.certificateService().initVerification(ephKey, method).awaitResponse()
            when (result.isSuccessful) {
                true -> {
                    EventsManager.sendEvent(EventAction.VERIFICATION_CODE_SENT)
                    LOG.d("onSendCode", "email sent !")
                }
                false -> {
                    EventsManager.sendEvent(EventAction.VERIFICATION_CODE_FAILED_SENDING)
                    LOG.d("onSendCode", "error : " + result.message())
                }
            }
        }
    }

    /**
     * callback to verify the code entered
     * @param code  code entered by the user, to validate
     */
    fun verifyCode(code: String) {
        jobs += GlobalScope.launch(Dispatchers.Main) {

            val verifyRequest = ImmutableVerificationRequest.builder()
                    .ephemeralKey(Doordeck.getKeys().private)
                    .verificationCode(code)
                    .build()
            val result: Result<CertificateChain> = client!!.certificateService().attemptVerification(verifyRequest).awaitResult()
            when (result) {
                is Result.Ok -> {
                    Doordeck.certificateChain = result.value
                    Doordeck.status = AuthStatus.AUTHORIZED
                    EventsManager.sendEvent(EventAction.CODE_VERIFICATION_SUCCESS)
                    view?.succeed()
                }
                is Result.Error -> {
                    EventsManager.sendEvent(EventAction.CODE_VERIFICATION_FAILED, result.exception)
                    LOG.e(TAG, "verifyCode error : " + result.exception)
                }
                is Result.Exception -> {
                    EventsManager.sendEvent(EventAction.SDK_NETWORK_ERROR, result.exception)
                    LOG.e(TAG, "verifyCode exception : " + result.exception)
                }
            }
        }
    }
}
