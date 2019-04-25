package com.doordeck.sdk.dto.operation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MutateDoorState.class, name = "MUTATE_LOCK"),
    @JsonSubTypes.Type(value = ImmutableMutateDoorState.class, name = "MUTATE_LOCK"),
    @JsonSubTypes.Type(value = AddUserOperation.class, name = "ADD_USER"),
    @JsonSubTypes.Type(value = ImmutableAddUserOperation.class, name = "ADD_USER"),
    @JsonSubTypes.Type(value = RemoveUserOperation.class, name="REMOVE_USER"),
    @JsonSubTypes.Type(value = ImmutableRemoveUserOperation.class, name="REMOVE_USER"),
    @JsonSubTypes.Type(value = MutateSetting.class, name = "MUTATE_SETTING"),
    @JsonSubTypes.Type(value = ImmutableMutateSetting.class, name = "MUTATE_SETTING")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Operation {

}
