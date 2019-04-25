package com.doordeck.sdk.jackson.deserializer;

import com.doordeck.sdk.util.BouncyCastleSingleton;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class PrivateKeyDeserializer extends StdDeserializer<PrivateKey> {

    public PrivateKeyDeserializer() {
        super(PrivateKey.class);
    }

    public PrivateKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            byte[] encodedKey = p.getBinaryValue();
            KeyFactory kf = KeyFactory.getInstance("Ed25519", BouncyCastleSingleton.getInstance());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encodedKey);
            return kf.generatePrivate(spec);
        } catch (Exception var5) {
            throw new IOException("Unable to parse Ed25519 privateKey key", var5);
        }
    }
}
