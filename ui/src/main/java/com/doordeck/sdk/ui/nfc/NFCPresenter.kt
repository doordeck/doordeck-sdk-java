package com.doordeck.sdk.ui.nfc

import android.content.Intent


/**
 * Logic for the NFC reader
 */
internal class NFCPresenter {

    private var view: NFCView? = null

    fun onStart(view: NFCView) {
        this.view = view
    }

    /**
     * clean memory
     */
    fun onStop() {
        this.view = null
    }

    /**
     * Process the intent got once the NFC has been discovered to get the tileId
     */
    fun processNFCData(intent: Intent) {
        NFCManager.handleNFCIntent(intent, object : NFCManager.Callback {
            override fun onReadSuccess(value: String?) {
                value?.let {
                    view?.unlockFromTileId(value)
                }

            }

            override fun onError(message: String?) {
                message?.let { view?.onError(it) }
            }
        })
    }


}
