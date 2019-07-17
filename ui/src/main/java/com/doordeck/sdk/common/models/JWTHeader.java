package com.doordeck.sdk.common.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;

import org.immutables.value.Value;

//  JWT header
@Value.Immutable
@JsonSerialize(as = ImmutableJWTHeader.class)
@JsonDeserialize(as = ImmutableJWTHeader.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public interface JWTHeader {

    String sub();
    String iss();
    int exp();
    int iat();

    @JsonProperty("auth_time")
    Optional<Integer> authTime();

    Optional<String> aud();
    Optional<String> sid();
    Optional<String> email();

    @JsonProperty("email_verified")
    Optional<Boolean> emailVerified();

    Optional<String> telephone();

    @JsonProperty("telephone_verified")
    Optional<Boolean> telephoneVerified();

    Optional<String> name();

    @JsonProperty("given_name")
    Optional<String> givenName();

    @JsonProperty("family_name")
    Optional<String> familyName();

    @JsonProperty("middle_name")
    Optional<String> middleName();

}
