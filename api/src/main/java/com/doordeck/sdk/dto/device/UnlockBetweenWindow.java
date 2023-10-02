package com.doordeck.sdk.dto.device;

import com.doordeck.sdk.util.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableUnlockBetweenWindow.class)
@JsonDeserialize(as = ImmutableUnlockBetweenWindow.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class UnlockBetweenWindow {

    @JsonFormat(pattern = "HH:mm")
    public abstract LocalTime start();

    @JsonFormat(pattern = "HH:mm")
    public abstract LocalTime end();

    public abstract ZoneId timezone();
    public abstract Set<DayOfWeek> days();

    @JsonFormat(pattern = "yyyy-MM-dd")
    public abstract Set<LocalDate> exceptions();

    @Value.Check
    protected void validate() {
        if (days().isEmpty()) {
            throw new IllegalArgumentException("Days must be specified");
        }

        if (start().isAfter(end())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

}
