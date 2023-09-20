package com.doordeck.sdk.dto.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateDeviceRequest.class)
@JsonDeserialize(as = ImmutableUpdateDeviceRequest.class)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public interface UpdateDeviceRequest {

    Optional<String> name(); // Alias
    Optional<Boolean> favourite();
    Optional<String> colour();

}
