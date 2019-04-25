package com.doordeck.sdk.common.manager

import com.doordeck.sdk.jackson.deserializer.Ed25519PublicKeyDeserializer
import com.doordeck.sdk.jackson.deserializer.PrivateKeyDeserializer
import com.doordeck.sdk.jackson.serializer.PrivateKeySerializer
import com.doordeck.sdk.jackson.serializer.PublicKeySerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import de.adorsys.android.securestoragelibrary.SecurePreferences
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey


internal class Datastore {

    private val om: ObjectMapper by lazy {
        val obj = ObjectMapper()
        val module = SimpleModule()
        module.addSerializer(PublicKey::class.java, PublicKeySerializer())
        module.addSerializer(PrivateKey::class.java, PrivateKeySerializer())
        module.addDeserializer(PublicKey::class.java, Ed25519PublicKeyDeserializer())
        module.addDeserializer(PrivateKey::class.java, PrivateKeyDeserializer())
        obj.registerModule(module)
    }


    // store pub/priv key in the safe android keychain
    fun saveKeyPair(keyPair: KeyPair) {
        SecurePreferences.setValue(PUB_KEY, om.writeValueAsString(keyPair.public))
        SecurePreferences.setValue(PRIV_KEY, om.writeValueAsString(keyPair.private))
    }


    // retrieve pub/priv key in the safe android keychain
    fun getKeyPair(): KeyPair? {
        val pubKeyStr = SecurePreferences.getStringValue(PUB_KEY, null)
        val privKeyStr = SecurePreferences.getStringValue(PRIV_KEY, null)
        if (pubKeyStr == null || privKeyStr == null)
            return null
        val pubKey = om.readValue(pubKeyStr, PublicKey::class.java)
        val privKey = om.readValue(privKeyStr, PrivateKey::class.java)
        return KeyPair(pubKey, privKey)
    }

    companion object {
        private const val PUB_KEY = "pub_key"
        private const val PRIV_KEY = "priv_key"
    }

}
