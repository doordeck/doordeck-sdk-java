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

import com.doordeck.sdk.core.jackson.Jackson;
import com.doordeck.sdk.jwt.Claims;
import com.doordeck.sdk.jwt.Header;
import com.doordeck.sdk.jwt.JOSEException;
import com.doordeck.sdk.jwt.Claims;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.io.BaseEncoding;

public abstract class BaseSigner {

    private final ObjectWriter payloadWriter;
    private final ObjectWriter headerWriter;

    public BaseSigner() {
        this(Jackson.sharedObjectMapper());
    }

    public BaseSigner(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            objectMapper = Jackson.sharedObjectMapper();
        }

        this.payloadWriter = objectMapper.writerFor(Claims.class);
        this.headerWriter = objectMapper.writerFor(Header.class);
    }

    protected String serialize(Header header, Claims payload) {
        try {
            byte[] headerJson = headerWriter.writeValueAsBytes(header);
            byte[] payloadJson = payloadWriter.writeValueAsBytes(payload);

            BaseEncoding encoder = BaseEncoding.base64Url().omitPadding();
            String serializedHeader = encoder.encode(headerJson);
            String serializedPayload = encoder.encode(payloadJson);

            return String.format("%s.%s", serializedHeader, serializedPayload);
        } catch (JsonProcessingException e) {
            throw new JOSEException(e);
        }
    }

}
