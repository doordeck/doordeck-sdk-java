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

package com.doordeck.sdk.jwt.signer;

import com.doordeck.sdk.jwt.Claims;
import com.doordeck.sdk.jwt.Header;
import com.doordeck.sdk.jwt.JOSEException;
import com.doordeck.sdk.jwt.SupportedAlgorithm;
import com.doordeck.sdk.util.BouncyCastleSingleton;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;

public class Ed25519Signer extends BaseSigner {

    public Ed25519Signer() {
        this(null);
    }

    public Ed25519Signer(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public String sign(Header header, Claims payload, PrivateKey privateKey) {
        if (!header.algorithm().equals(SupportedAlgorithm.EdDSA)) {
            throw new IllegalArgumentException("Header must specified algorithm as EdDSA");
        }

        try {
            String serialized = serialize(header, payload);
            Signature signer = Signature.getInstance("EdDSA", BouncyCastleSingleton.getInstance());
            signer.initSign(privateKey);
            signer.update(serialized.getBytes(StandardCharsets.UTF_8));

            byte[] jwsSignature = signer.sign();
            String signature = BaseEncoding.base64Url().omitPadding().encode(jwsSignature);

            return serialized + "." + signature;
        } catch (GeneralSecurityException e) {
            throw new JOSEException(e);
        }
    }

}

