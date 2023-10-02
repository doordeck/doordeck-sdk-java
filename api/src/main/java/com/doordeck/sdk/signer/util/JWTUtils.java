package com.doordeck.sdk.signer.util;

import com.doordeck.sdk.dto.operation.Operation;
import com.doordeck.sdk.jackson.Jackson;
import com.doordeck.sdk.jwt.Claims;
import com.doordeck.sdk.jwt.Header;
import com.doordeck.sdk.jwt.ImmutableClaims;
import com.doordeck.sdk.jwt.ImmutableHeader;
import com.doordeck.sdk.jwt.SupportedAlgorithm;
import com.doordeck.sdk.jwt.signer.Ed25519Signer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JWTUtils {

    public static String getSignedJWT(List<X509Certificate> certs, PrivateKey key, UUID deviceId, UUID userId, Operation operation) {

        ObjectMapper objectMapper = Jackson.sharedObjectMapper();
        Ed25519Signer signer = new Ed25519Signer(objectMapper);

        Instant now = Instant.now();
        Claims claims = ImmutableClaims.builder()
                .deviceId(deviceId)
                .userId(userId)
                .notBefore(now)
                .expiresAt(now.plus(Duration.ofSeconds(60)))
                .operation(operation)
                .build();

        Header header = ImmutableHeader.builder()
                .algorithm(SupportedAlgorithm.EdDSA)
                .certificateChain(certs)
                .build();

        return signer.sign(header, claims, key);
    }
}
