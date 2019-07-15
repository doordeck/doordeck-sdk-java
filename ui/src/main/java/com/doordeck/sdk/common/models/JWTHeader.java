package com.doordeck.sdk.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;

import org.immutables.value.Value;

import java.net.URI;
import java.sql.Time;
import java.util.Locale;
import java.util.TimeZone;

//  JWT header
@Value.Immutable
@JsonSerialize(as = ImmutableJWTHeader.class)
@JsonDeserialize(as = ImmutableJWTHeader.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface JWTHeader {

    String sub();
    String iss();
    String session();
    Boolean refresh();
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

    Optional<Locale> locale();

    @JsonProperty("zoneinfo")
    Optional<TimeZone> zoneInfo();

    Optional<String> name();

    @JsonProperty("given_name")
    Optional<String> givenName();

    @JsonProperty("family_name")
    Optional<String> familyName();

    @JsonProperty("middle_name")
    Optional<String> middleName();

    Optional<URI> picture();

}
