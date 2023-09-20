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
            val result: Response<Map<String, String>> = client!!.certificateService().initVerification(ephKey).awaitResponse()
            when (result.isSuccessful) {
                true -> {
                    val usedMethod = result.body()?.get("method");
                    Doordeck.jwtToken?.let { header ->
                        when (usedMethod?.let { VerificationMethod.valueOf(it) }) {
                            VerificationMethod.SMS -> {
                                method = VerificationMethod.SMS
                                view?.setPhoneNumber(header.telephone().get())
                            }
                            VerificationMethod.EMAIL -> {
                                method = VerificationMethod.EMAIL
                                view?.setEmail(header.email().get())
                            }
                            VerificationMethod.TELEPHONE -> {
                                method = VerificationMethod.TELEPHONE
                                view?.setPhoneNumber(header.telephone().get())
                            }
                            VerificationMethod.WHATSAPP -> {
                                method = VerificationMethod.WHATSAPP
                                view?.setPhoneNumberWhatsapp(header.telephone().get())
                            }
                            else -> view?.noMethodDefined()

                        }
                    }
                    EventsManager.sendEvent(EventAction.VERIFICATION_CODE_SENT)
                    LOG.d("onSendCode", usedMethod + "sent !")
                    view?.verifyCodeSuccess()
                }
                false -> {
                    EventsManager.sendEvent(EventAction.VERIFICATION_CODE_FAILED_SENDING)
                    LOG.d("onSendCode", "error : " + result.message())
                    view?.verifyCodeFail()
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
                    Doordeck.storeLaststatus(Doordeck.status)
                    EventsManager.sendEvent(EventAction.CODE_VERIFICATION_SUCCESS)
                    view?.succeed()
                }
                is Result.Error -> {
                    EventsManager.sendEvent(EventAction.CODE_VERIFICATION_FAILED, result.exception)
                    LOG.e(TAG, "verifyCode error : " + result.exception)
                    view?.fail()
                }
                is Result.Exception -> {
                    EventsManager.sendEvent(EventAction.SDK_NETWORK_ERROR, result.exception)
                    LOG.e(TAG, "verifyCode exception : " + result.exception)
                }
            }
        }
    }
}
