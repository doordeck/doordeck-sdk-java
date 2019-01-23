package com.doordeck.sdk.signer;

import com.doordeck.sdk.core.dto.ImmutableMutateDoorState;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class SignedOperationFactoryTest {

    private static final UUID USER_ID = UUID.fromString("f7407cf1-0194-425d-88b7-5af2c8cead47");
    private static final UUID DEVICE_ID = UUID.fromString("6c426fd9-51fb-443f-854a-294e1a7de759");

    @Test
    public void testRSASignatureGeneration() throws NoSuchAlgorithmException, JOSEException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048); // Matches legacy keys
        KeyPair keyPair = kpg.generateKeyPair();

        String signedJwt = ImmutableSignedOperationFactory.builder()
                .deviceId(DEVICE_ID)
                .userId(USER_ID)
                .operation(ImmutableMutateDoorState.builder().locked(false).build())
                .build()
                .sign(keyPair.getPrivate());

        System.out.println(signedJwt);
    }

}
