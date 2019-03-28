package com.doordeck.sdk.dto.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableDeviceUsageRequirements.class)
@JsonDeserialize(as = ImmutableDeviceUsageRequirements.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface DeviceUsageRequirements {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    Optional<LocationRequirement> location();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Set<TimeRequirement> time();

}
