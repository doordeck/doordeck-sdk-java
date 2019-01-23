package com.doordeck.sdk.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

import static com.doordeck.sdk.core.util.Preconditions.checkArgument;

@Value.Immutable
@JsonSerialize(as = ImmutableUnlockBetweenWindow.class)
@JsonDeserialize(as = ImmutableUnlockBetweenWindow.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface UnlockBetweenWindow {

    @JsonFormat(pattern = "HH:mm")
    LocalTime start();

    @JsonFormat(pattern = "HH:mm")
    LocalTime end();

    ZoneId timezone();
    Set<DayOfWeek> days();

    @JsonFormat(pattern = "yyyy-MM-dd")
    Set<LocalDate> exceptions();

    @Value.Check
    default void validate() {
        checkArgument(!days().isEmpty(), "Days must be specified");
        checkArgument(end().equals(start()) || start().isBefore(end()), "Start time must be before end time");
    }

}
