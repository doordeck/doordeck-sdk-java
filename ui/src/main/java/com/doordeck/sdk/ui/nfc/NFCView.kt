package com.doordeck.sdk.ui.nfc

internal interface NFCView  {
    fun unlockFromTileId(tileId: String)
    fun onError(message: String)
}
