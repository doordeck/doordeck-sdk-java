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

import com.doordeck.sdk.signer.util.CertUtils;
import com.google.crypto.tink.KeysetHandle;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.util.Base64URL;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// FIXME this all needs to load from tink protected keystores
public class JsonWebKeyGenerator {

    public OctetKeyPair generateKeyPair(KeysetHandle keysetHandle) throws JOSEException {
        return new OctetKeyPairGenerator(Curve.Ed25519)
                .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key
                .keyOperations(Collections.singleton(KeyOperation.SIGN))
                .keyID(UUID.randomUUID().toString()) // give the key a unique ID
                .generate();
    }

    public OctetKeyPair loadKeyPair(byte[] privateKey, byte[] publicKey, List<X509Certificate> certificateChain) throws CertificateEncodingException {
        return new OctetKeyPair.Builder(Curve.Ed25519, Base64URL.encode(privateKey))
                .d(Base64URL.encode(publicKey))
                .keyUse(KeyUse.SIGNATURE)
                .keyOperations(Collections.singleton(KeyOperation.SIGN))
                .algorithm(JWSAlgorithm.EdDSA)
                .x509CertChain(CertUtils.asBase64(certificateChain))
                .build();
    }

}
