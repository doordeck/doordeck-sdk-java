package com.doordeck.sdk.core.dto.site;

import com.doordeck.sdk.core.jackson.deserializer.InstantSecondDeserializer;
import com.doordeck.sdk.core.jackson.serializer.InstantSecondSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;
import org.joda.time.Instant;

import java.net.URI;
import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableSite.class)
@JsonDeserialize(as = ImmutableSite.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Site {

    @JsonProperty("id")
    UUID siteId();

    String name();
    String colour();
    Optional<URI> logoUrl();

    Optional<Double> longitude();
    Optional<Double>  latitude();
    Optional<Integer> radius();

    Optional<UUID> createdBy();

    @JsonProperty("created")
    @JsonSerialize(using = InstantSecondSerializer.class)
    @JsonDeserialize(using = InstantSecondDeserializer.class)
    Instant createdAt();

    @JsonProperty("updated")
    @JsonSerialize(using = InstantSecondSerializer.class)
    @JsonDeserialize(using = InstantSecondDeserializer.class)
    Instant updatedAt();


}
