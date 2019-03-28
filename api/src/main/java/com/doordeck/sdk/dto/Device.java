package com.doordeck.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableDevice.class)
@JsonDeserialize(as = ImmutableDevice.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Device {

    @JsonProperty("id")
    UUID deviceId();

    String name();

    boolean favourite();

}
