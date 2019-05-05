package com.doordeck.sdk.common.manager

import com.doordeck.sdk.jackson.Jackson
import de.adorsys.android.securestoragelibrary.SecurePreferences
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey


internal class Datastore {

    // store pub/priv key in the safe android keychain
    fun saveKeyPair(keyPair: KeyPair) {
        val om = Jackson.sharedObjectMapper()
        SecurePreferences.setValue(PUB_KEY, om.writeValueAsString(keyPair.public))
        SecurePreferences.setValue(PRIV_KEY, om.writeValueAsString(keyPair.private))
    }


    // retrieve pub/priv key in the safe android keychain
    fun getKeyPair(): KeyPair? {
        val pubKeyStr = SecurePreferences.getStringValue(PUB_KEY, null)
        val privKeyStr = SecurePreferences.getStringValue(PRIV_KEY, null)
        if (pubKeyStr == null || privKeyStr == null)
            return null
        val om = Jackson.sharedObjectMapper()
        val pubKey = om.readValue(pubKeyStr, PublicKey::class.java)
        val privKey = om.readValue(privKeyStr, PrivateKey::class.java)
        return KeyPair(pubKey, privKey)
    }

    public fun clean() {
        SecurePreferences.clearAllValues()
    }

    companion object {
        private const val PUB_KEY = "pub_key"
        private const val PRIV_KEY = "priv_key"
    }

}
