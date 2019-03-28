package com.doordeck.sdk.dto;

import com.doordeck.sdk.util.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Value.Immutable
@JsonSerialize(as = ImmutableUnlockBetweenWindow.class)
@JsonDeserialize(as = ImmutableUnlockBetweenWindow.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class UnlockBetweenWindow {

    @JsonFormat(pattern = "HH:mm")
    public abstract LocalTime start();

    @JsonFormat(pattern = "HH:mm")
    public abstract LocalTime end();

    public abstract DateTimeZone timezone();
    public abstract Set<DayOfWeek> days();

    @JsonFormat(pattern = "yyyy-MM-dd")
    public abstract Set<LocalDate> exceptions();

    @Value.Check
    protected void validate() {
        checkArgument(!days().isEmpty(), "Days must be specified");
        checkArgument(end().equals(start()) || start().isBefore(end()), "Start time must be before end time");
    }

}
