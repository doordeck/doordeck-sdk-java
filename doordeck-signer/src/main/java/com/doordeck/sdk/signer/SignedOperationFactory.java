/*
 * Copyright 2019 Doordeck Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doordeck.sdk.signer;

import com.doordeck.sdk.core.dto.Operation;
import com.doordeck.sdk.core.jackson.Jackson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.immutables.value.Value;

import java.io.IOException;
import java.security.PrivateKey;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.doordeck.sdk.core.util.Preconditions.checkArgument;

@Value.Immutable
public interface SignedOperationFactory {

    Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    Duration MAX_EXPIRY = Duration.ofDays(14);

    UUID userId();
    UUID deviceId();
    Operation operation();

    @Value.Default
    default Instant notBefore() {
        return Instant.now();
    }

    @Value.Default
    default Instant expiresAt() {
        return Instant.now().plus(DEFAULT_TIMEOUT);
    }

    @Value.Check
    default void validate() {
        checkArgument(Instant.now().plus(MAX_EXPIRY).compareTo(expiresAt()) >= 0, "Expiry must be in less than 14 days time");
    }

    @Value.Derived
    default String uniqueIdentifier() {
        return UUID.randomUUID().toString();
    }

    @Value.Derived
    default JWTClaimsSet asClaimSet() {
        try {
            byte[] operationJson = Jackson.sharedObjectMapper().writeValueAsBytes(operation());
            Map<String, Object> operationMap = Jackson.sharedObjectMapper().readValue(operationJson, new TypeReference<Map<String, Object>>() {
            });

            return new JWTClaimsSet.Builder()
                    .subject(deviceId().toString())
                    .issuer(userId().toString())
                    .expirationTime(Date.from(expiresAt()))
                    .notBeforeTime(Date.from(notBefore()))
                    .jwtID(uniqueIdentifier())
                    .issueTime(Date.from(Instant.now()))
                    .claim("operation", operationMap)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse operation");
        }
    }

    default String sign(OctetKeyPair signingKey) throws JOSEException {
        checkArgument(signingKey.getX509CertChain().size() == 4, "Missing required number of certificates");
        checkArgument(Curve.Ed25519.equals(signingKey.getCurve()), "Only Ed25519 curve supported");

        JWTClaimsSet claimsSet = asClaimSet();

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                .keyID(signingKey.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);

        // Create the EdDSA signer with the generated OKP
        JWSSigner signer = new Ed25519Signer(signingKey);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    @Deprecated // Use Ed25519 signer instead
    default String sign(PrivateKey signingKey) throws JOSEException {
        JWTClaimsSet claimsSet = asClaimSet();

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);

        RSASSASigner signer = new RSASSASigner(signingKey);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

}
