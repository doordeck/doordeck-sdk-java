package com.doordeck.sdk.dto.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLocationRequirement.class)
@JsonDeserialize(as = ImmutableLocationRequirement.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface LocationRequirement {

    double latitude();
    double longitude();
    int radius();
    int accuracy();

    boolean enabled();

}
