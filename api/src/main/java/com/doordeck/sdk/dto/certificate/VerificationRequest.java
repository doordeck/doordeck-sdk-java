package com.doordeck.sdk.dto.certificate;

import com.doordeck.sdk.util.BouncyCastleSingleton;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;

@Value.Immutable
@JsonSerialize(as = ImmutableVerificationRequest.class)
@JsonDeserialize(as = ImmutableVerificationRequest.class)
public interface VerificationRequest {

    @JsonIgnore
    PrivateKey ephemeralKey();

    @JsonIgnore
    String verificationCode();

    @Value.Derived
    default byte[] verificationSignature() {
        try {
            Signature sign = Signature.getInstance("EdDSA", BouncyCastleSingleton.getInstance());
            sign.initSign(ephemeralKey());
            sign.update(verificationCode().getBytes(StandardCharsets.UTF_8));
            return sign.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Unable to calculate signature", e);
        }
    }

}
