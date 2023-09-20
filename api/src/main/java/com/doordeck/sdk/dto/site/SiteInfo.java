package com.doordeck.sdk.dto.site;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSiteInfo.class)
@JsonDeserialize(as = ImmutableSiteInfo.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface SiteInfo {

    Optional<String> icon();
    String text();

}
