package com.doordeck.sdk.ui.nfc

// interface between the activity and the presenter
internal interface NFCView  {
    fun unlockFromTileId(tileId: String)
    fun onError(message: String)
}
