package com.doordeck.sdk.common.manager

import com.doordeck.sdk.common.utils.SecurePreferencesHelper
import com.doordeck.sdk.dto.certificate.CertificateChain
import com.doordeck.sdk.jackson.Jackson
import de.adorsys.android.securestoragelibrary.SecurePreferences
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

// tore some data locally (in shared pref and/or android keychain)
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

    // store certificates in the safe android keychain
    fun saveCertificates (certificateChain: CertificateChain) {
        val om = Jackson.sharedObjectMapper()
        Doordeck.sharedPreference?.save(CERTS, om.writeValueAsString(certificateChain))
    }


    // retrieve certificates in the safe android keychain
    fun getSavedCertificates(): CertificateChain? {
        val certificateChainStr = Doordeck.sharedPreference?.getValueString(CERTS)
        if (certificateChainStr == null) return null
        val om = Jackson.sharedObjectMapper()
        val certificateChain = om.readValue(certificateChainStr, CertificateChain::class.java)
        return certificateChain
    }

    // store certificates in the safe android keychain
    fun saveAuthToken (authToken: String) {
        SecurePreferencesHelper.setLongStringValue(TOKEN, authToken)
    }


    // retrieve certificates in the safe android keychain
    fun getAuthToken(): String? {
        return SecurePreferencesHelper.getLongStringValue(TOKEN)
    }

    // store certificates in the safe android keychain
    fun saveStatus (status: AuthStatus) {
        val om = Jackson.sharedObjectMapper()
        Doordeck.sharedPreference?.save(TOKEN, om.writeValueAsString(status))
    }


    // retrieve certificates in the safe android keychain
    fun getStoredStatus(): AuthStatus? {
        val status = Doordeck.sharedPreference?.getValueString(TOKEN)
        if (status == null) return null
        val om = Jackson.sharedObjectMapper()
        return om.readValue(status, AuthStatus::class.java)
    }


    public fun clean() {
        SecurePreferences.clearAllValues()
        Doordeck.sharedPreference?.clearSharedPreference()
    }



    companion object {
        private const val PUB_KEY = "pub_key"
        private const val PRIV_KEY = "priv_key"
        private const val CERTS = "certs"
        private const val TOKEN = "autToken"
        private const val STATUS = "status"
    }



}
