package com.doordeck.sdk.dto.site;

import com.doordeck.sdk.jackson.deserializer.InstantSecondDeserializer;
import com.doordeck.sdk.jackson.serializer.InstantSecondSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import java.net.URI;
import java.util.List;
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
    Optional<DateTimeZone> timezone();
    Optional<OpenHours> openingHours();
    List<SiteInfo> information();

    Optional<URI> website();
    Optional<String> phoneNumber();
    Optional<String> email();

}
