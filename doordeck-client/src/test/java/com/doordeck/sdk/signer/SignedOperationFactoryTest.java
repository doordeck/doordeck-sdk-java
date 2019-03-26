package com.doordeck.sdk.signer;

import com.doordeck.sdk.core.dto.ImmutableMutateDoorState;
import com.doordeck.sdk.jwt.*;
import com.doordeck.sdk.jwt.signer.Ed25519Signer;
import com.doordeck.sdk.jwt.signer.RSASigner;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import java.security.*;
import java.util.UUID;

public class SignedOperationFactoryTest {

    private static final UUID USER_ID = UUID.fromString("f7407cf1-0194-425d-88b7-5af2c8cead47");
    private static final UUID DEVICE_ID = UUID.fromString("6c426fd9-51fb-443f-854a-294e1a7de759");

    private final KeyPair keyPair;

    public SignedOperationFactoryTest() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048); // Matches legacy keys
        this.keyPair = kpg.generateKeyPair();

        System.out.println(pemEncode(keyPair.getPublic()));
    }

    @Test
    public void testRSASignatureGeneration() {
        Header header = ImmutableHeader.builder()
                .algorithm(SupportedAlgorithm.RS256)
                .build();
        Claims payload = ImmutableClaims.builder()
                .deviceId(DEVICE_ID)
                .userId(USER_ID)
                .operation(ImmutableMutateDoorState.builder().locked(false).build())
                .build();

        String jwt = new RSASigner().sign(header, payload, this.keyPair.getPrivate());

        System.out.println(jwt);
    }

    @Test
    public void testEd25519SignatureGeneration() throws GeneralSecurityException  {
        Header header = ImmutableHeader.builder()
                .algorithm(SupportedAlgorithm.EdDSA)
                .build();
        Claims payload = ImmutableClaims.builder()
                .deviceId(DEVICE_ID)
                .userId(USER_ID)
                .operation(ImmutableMutateDoorState.builder().locked(false).build())
                .build();

        String jwt = new Ed25519Signer().sign(header, payload, Ed25519KeyGenerator.generate().getPrivate());

        System.out.println(jwt);
    }

    private static String pemEncode(PublicKey publicKey) {
        String header = "-----BEGIN PUBLIC KEY-----";
        String footer = "-----END PUBLIC KEY-----";

        String encodedKey = BaseEncoding.base64().withSeparator("\n", 64).encode(publicKey.getEncoded());

        return String.format("%s\n%s\n%s", header, encodedKey, footer);
    }

}
