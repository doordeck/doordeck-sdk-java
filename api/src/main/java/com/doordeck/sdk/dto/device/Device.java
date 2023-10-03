package com.doordeck.sdk.dto.device;

import com.doordeck.sdk.dto.Role;
import com.doordeck.sdk.jackson.deserializer.InstantSecondDeserializer;
import com.doordeck.sdk.jackson.serializer.InstantSecondSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableDevice.class)
@JsonDeserialize(as = ImmutableDevice.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Device {

    @JsonProperty("id")
    UUID deviceId();

    String name();

    @Nullable
    String colour();

    boolean favourite();

    @JsonSerialize(contentUsing = InstantSecondSerializer.class)
    @JsonDeserialize(contentUsing = InstantSecondDeserializer.class)
    Optional<Instant> start();

    @JsonSerialize(contentUsing = InstantSecondSerializer.class)
    @JsonDeserialize(contentUsing = InstantSecondDeserializer.class)
    Optional<Instant> end();

    Role role();
    DeviceSetting settings();

}
