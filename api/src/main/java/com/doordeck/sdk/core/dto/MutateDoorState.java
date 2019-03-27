package com.doordeck.sdk.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableMutateDoorState.class)
@JsonDeserialize(as = ImmutableMutateDoorState.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface MutateDoorState extends Operation {

    boolean locked();

}
