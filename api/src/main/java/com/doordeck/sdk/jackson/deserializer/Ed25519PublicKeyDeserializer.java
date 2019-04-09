package com.doordeck.sdk.jackson.deserializer;

import com.doordeck.sdk.util.BouncyCastleSingleton;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class Ed25519PublicKeyDeserializer extends StdDeserializer<PublicKey> {

    private static final int ED25519_KEY_SIZE = 32;

    /**
     * Singleton key factory provider
     */
    private enum Helper {
        INSTANCE;

        KeyFactory keyFactory;

        Helper() {
            try {
                this.keyFactory = KeyFactory.getInstance("Ed25519", BouncyCastleSingleton.getInstance());
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("BouncyCastle provider not loaded");
            }
        }
    }

    public Ed25519PublicKeyDeserializer() {
        super(PublicKey.class);
    }

    @Override
    public PublicKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            // The key maybe in 'raw' format or in DER encoded format
            byte[] encodedKey = p.getBinaryValue();

            if (encodedKey.length == ED25519_KEY_SIZE) {
                // Likely to be in raw format
                SubjectPublicKeyInfo pubKeyInfo = new SubjectPublicKeyInfo(
                    new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), encodedKey);
                encodedKey = pubKeyInfo.getEncoded(); // Re-encode using ASN.1
            }

            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(encodedKey);
            return Helper.INSTANCE.keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            throw new IOException("Unable to parse Ed25519 public key", e);
        }
    }
}
