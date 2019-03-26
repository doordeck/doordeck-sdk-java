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

package com.doordeck.sdk.core.dto;

import com.doordeck.sdk.core.jackson.deserializer.PublicKeyDeserializer;
import com.doordeck.sdk.core.jackson.serializer.PublicKeySerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;
import org.joda.time.Instant;

import java.security.PublicKey;
import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableAddUserOperation.class)
@JsonDeserialize(as = ImmutableAddUserOperation.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AddUserOperation implements Operation {

    public abstract UUID user();

    @JsonSerialize(using = PublicKeySerializer.class)
    @JsonDeserialize(using = PublicKeyDeserializer.class)
    public abstract PublicKey publicKey();

    @Value.Default
    public Role role() {
        return Role.USER;
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public abstract Optional<Instant> start();

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public abstract Optional<Instant> end();

}