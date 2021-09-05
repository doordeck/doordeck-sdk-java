package com.doordeck.sdk.dto.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;
import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableMultiDeviceResponse.class)
@JsonDeserialize(as = ImmutableMultiDeviceResponse.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface MultiDeviceResponse {

    @JsonProperty("deviceIds")
    List<UUID> deviceIds();

}
