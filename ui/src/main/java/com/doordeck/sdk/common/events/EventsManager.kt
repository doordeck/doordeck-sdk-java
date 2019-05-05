package com.doordeck.sdk.common.events

import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.models.DDEVENT
import com.doordeck.sdk.common.models.EventAction
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


/**
 * Events Manager : responsible for sending the event for those who subscribe to them,
 * either using rx or a callback
 */
internal object EventsManager {
    private val publishErrorObject = PublishSubject.create<DDEVENT>()
    private val errorObservable = publishErrorObject.hide()


    fun sendEvent(currentEventAction: EventAction, exception: String) {
        val event = DDEVENT(currentEventAction, exception)
        send(event)
    }

    fun sendEvent(currentEventAction: EventAction, exception: Exception) {
        val event = DDEVENT(currentEventAction, exception.localizedMessage)
        send(event)
    }

    fun sendEvent(currentEventAction: EventAction, throwable: Throwable) {
        val event = DDEVENT(currentEventAction, throwable.localizedMessage)
        send(event)
    }

    fun sendEvent(currentEventAction: EventAction) {
        val event = DDEVENT(currentEventAction)
        send(event)
    }

    private fun send(event: DDEVENT) {
        publishErrorObject.onNext(event)
        callCallback(event.event)
    }

    internal fun eventsObservable(): Observable<DDEVENT> {
        return errorObservable
    }

    private fun callCallback(error: EventAction) {
        val callback = Doordeck.callback
        val unlockCallback = Doordeck.unlockCallback
        when (error) {
            EventAction.NO_INTERNET -> callback?.noInternet()
            EventAction.SDK_ERROR -> callback?.sdkError()
            EventAction.VERIFICATION_CODE_SENT -> callback?.verificationCodeSent()
            EventAction.VERIFICATION_CODE_FAILED_SENDING -> callback?.verificationCodeFailedSending()
            EventAction.CODE_VERIFICATION_SUCCESS -> callback?.codeVerificationSuccess()
            EventAction.CODE_VERIFICATION_FAILED -> callback?.codeVerificationFailed()
            EventAction.SDK_NETWORK_ERROR -> callback?.networkError()
            EventAction.UNLOCK_INVALID_TILE_ID -> callback?.unlockedInvalidTileID()
            EventAction.AUTHENTICATED -> callback?.authentificationSuccess()
            EventAction.UNLOCK_SUCCESS -> {
                callback?.unlockSuccess()
                unlockCallback?.unlockSuccess()
            }
            EventAction.UNLOCK_FAILED -> {
                callback?.unlockFailed()
                unlockCallback?.unlockFailed()
            }
            EventAction.RESOLVE_TILE_FAILED -> callback?.resolveTileFailed()
            EventAction.RESOLVE_TILE_SUCCESS -> callback?.resolveTileSuccess()
            EventAction.CLOSE_QR_CODE_VIEW -> {
            }
            EventAction.CLOSE_NFC_VIEW -> {
            }
        }
    }


}
