package com.doordeck.sdk.dto.site;

import com.doordeck.sdk.jackson.deserializer.InstantSecondDeserializer;
import com.doordeck.sdk.jackson.serializer.InstantSecondSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableSite.class)
@JsonDeserialize(as = ImmutableSite.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Site {

    UUID id();
    String name();
    Optional<UUID> createdBy();
    String colour();

    Optional<Double> longitude();
    Optional<Double> latitude();
    Optional<Integer> radius();

    Optional<URI> passBackground();

    @JsonSerialize(using = InstantSecondSerializer.class)
    @JsonDeserialize(using = InstantSecondDeserializer.class)
    Instant created();

    @JsonSerialize(using = InstantSecondSerializer.class)
    @JsonDeserialize(using = InstantSecondDeserializer.class)
    Instant updated();

    Optional<String> googlePlaceId();
    Optional<String> address();
    Optional<ZoneId> timezone();
    List<SiteInfo> information();

    Optional<URI> website();
    Optional<String> phoneNumber();
    Optional<String> email();

}
