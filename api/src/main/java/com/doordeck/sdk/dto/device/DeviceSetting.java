package com.doordeck.sdk.dto.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;

import java.util.Set;
import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableDeviceSetting.class)
@JsonDeserialize(as = ImmutableDeviceSetting.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Style(depluralize = true)
public interface DeviceSetting {

    @JsonProperty("unlockBetweenWindow")
    Optional<UnlockBetweenWindow> unlockBetween();

    String defaultName();

    Set<UUID> tiles();

    @Value.Default
    default boolean hidden() {
        return false;
    }

    @Value.Default
    default DeviceUsageRequirements usageRequirements() {
        return ImmutableDeviceUsageRequirements.builder().build();
    }

}
