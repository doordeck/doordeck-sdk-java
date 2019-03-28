package com.doordeck.sdk.dto.operation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOperationWrapper.class)
@JsonDeserialize(as = ImmutableOperationWrapper.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class OperationWrapper {

    public abstract Operation operation();

    static OperationWrapper of(Operation operation) {
        return ImmutableOperationWrapper.builder().operation(operation).build();
    }

}
