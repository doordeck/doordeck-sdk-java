package com.doordeck.sdk.dto.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableShareableDevice.class)
@JsonDeserialize(as = ImmutableShareableDevice.class)
public interface ShareableDevice {

    @JsonProperty("id")
    UUID deviceId();
    String name();

}
