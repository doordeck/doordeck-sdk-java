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

package com.doordeck.sdk.jwt;

import com.doordeck.sdk.jackson.deserializer.DERCertificateDeserializer;
import com.doordeck.sdk.jackson.serializer.DERCertificateSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableHeader.class)
@JsonDeserialize(as = ImmutableHeader.class)
public abstract class Header {

    private static final String JWT_TYPE = "jwt";

    @JsonProperty("alg")
    public abstract SupportedAlgorithm algorithm();

    @Value.Default
    @JsonProperty("typ")
    public String type() {
        return JWT_TYPE;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(contentUsing = DERCertificateSerializer.class)
    @JsonDeserialize(contentUsing = DERCertificateDeserializer.class)
    @JsonProperty("x5c")
    public abstract List<X509Certificate> certificateChain();

    @JsonProperty("kid")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public abstract Optional<String> keyId();

}
