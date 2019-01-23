package com.doordeck.sdk.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOperationWrapper.class)
@JsonDeserialize(as = ImmutableOperationWrapper.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface OperationWrapper {

    Operation operation();

    static OperationWrapper of(Operation operation) {
        return ImmutableOperationWrapper.builder().operation(operation).build();
    }

}
