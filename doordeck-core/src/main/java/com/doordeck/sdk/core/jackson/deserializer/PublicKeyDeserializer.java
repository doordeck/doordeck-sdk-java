package com.doordeck.sdk.core.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class PublicKeyDeserializer extends JsonDeserializer<PublicKey> {

    private enum KeyFactoryInstance {
        INSTANCE;

        private KeyFactory keyFactory;

        KeyFactoryInstance() {
            try {
                this.keyFactory = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        public KeyFactory keyFactory() {
            return keyFactory;
        }
    }

    @Override
    public PublicKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(p.getBinaryValue());
            return KeyFactoryInstance.INSTANCE.keyFactory().generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new IOException("Unable to parse DER encoded public key", e);
        }
    }

}
