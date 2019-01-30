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

import com.doordeck.sdk.core.dto.Operation;
import com.doordeck.sdk.core.jackson.deserializer.InstantSecondDeserializer;
import com.doordeck.sdk.core.jackson.serializer.InstantSecondSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Value.Immutable
@JsonSerialize(as = ImmutableClaims.class)
@JsonDeserialize(as = ImmutableClaims.class)
public abstract class Claims {

    private static final Duration DEFAULT_TIMEOUT = Duration.standardSeconds(60);
    private static final Duration MAX_EXPIRY = Duration.standardDays(14);

    @JsonProperty("iss")
    public abstract UUID userId();

    @JsonProperty("sub")
    public abstract UUID deviceId();

    @JsonProperty("iat")
    @Value.Derived
    @JsonSerialize(using = InstantSecondSerializer.class)
    @JsonDeserialize(using = InstantSecondDeserializer.class)
    public Instant issuedAt() {
        return Instant.now();
    }

    @Value.Default
    @JsonProperty("nbf")
    @JsonSerialize(using = InstantSecondSerializer.class)
    @JsonDeserialize(using = InstantSecondDeserializer.class)
    public Instant notBefore() {
        return Instant.now();
    }

    @Value.Default
    @JsonProperty("exp")
    @JsonSerialize(using = InstantSecondSerializer.class)
    @JsonDeserialize(using = InstantSecondDeserializer.class)
    public Instant expiresAt() {
        return Instant.now().plus(DEFAULT_TIMEOUT);
    }


    @Value.Derived
    @JsonProperty("jti")
    public String uniqueIdentifier() {
        return UUID.randomUUID().toString();
    }

    public abstract Operation operation();

    @Value.Check
    protected void validate() {
        checkArgument(Instant.now().plus(MAX_EXPIRY).compareTo(expiresAt()) >= 0, "Expiry must be in less than 14 days time");
    }

}
