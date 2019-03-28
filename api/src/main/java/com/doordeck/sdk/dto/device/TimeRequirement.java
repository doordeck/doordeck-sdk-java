package com.doordeck.sdk.dto.device;

import com.doordeck.sdk.util.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableTimeRequirement.class)
@JsonDeserialize(as = ImmutableTimeRequirement.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TimeRequirement {

    @JsonFormat(pattern = "HH:mm")
    LocalTime start();

    @JsonFormat(pattern = "HH:mm")
    LocalTime end();

    DateTimeZone timezone();
    Set<DayOfWeek> days();

}
