package com.doordeck.sdk.common.manager

import android.content.Context
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
    fun saveKeyPair(context: Context, keyPair: KeyPair) {
        val om = Jackson.sharedObjectMapper()
        SecurePreferences.setValue(PUB_KEY, om.writeValueAsString(keyPair.public), context)
        SecurePreferences.setValue(PRIV_KEY, om.writeValueAsString(keyPair.private), context)
    }


    // retrieve pub/priv key in the safe android keychain
    fun getKeyPair(context: Context): KeyPair? {
        val pubKeyStr = SecurePreferences.getStringValue(PUB_KEY, context, null)
        val privKeyStr = SecurePreferences.getStringValue(PRIV_KEY, context, null)
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
    fun saveAuthToken (context: Context, authToken: String) {
        SecurePreferencesHelper.setLongStringValue(context, TOKEN, authToken)
    }


    // retrieve certificates in the safe android keychain
    fun getAuthToken(context: Context): String? {
        return SecurePreferencesHelper.getLongStringValue(context, TOKEN)
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


    public fun clean(context: Context) {
        SecurePreferences.clearAllValues(context)
        Doordeck.sharedPreference?.clearSharedPreference()
    }

    fun saveTheme(darkMode: Boolean) {
        Doordeck.sharedPreference?.save(DARKMODE, darkMode)
    }

    fun getSavedTheme(): Boolean? {
        return Doordeck.sharedPreference?.getValueBoolean(DARKMODE, false)
    }


    companion object {
        private const val PUB_KEY = "pub_key"
        private const val PRIV_KEY = "priv_key"
        private const val CERTS = "certs"
        private const val TOKEN = "autToken"
        private const val DARKMODE = "darkMode"
        private const val STATUS = "status"
    }



}
