package com.doordeck.sdk.dto.site;

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
    double longitude();
    double latitude();
    int radius();
    URI passBackground();

    Instant created();
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
