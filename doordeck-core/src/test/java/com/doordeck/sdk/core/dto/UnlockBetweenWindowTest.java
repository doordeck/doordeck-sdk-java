package com.doordeck.sdk.core.dto;

import com.doordeck.sdk.core.jackson.Jackson;
import com.doordeck.sdk.core.util.DayOfWeek;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.Test;

import static com.doordeck.sdk.core.util.FixtureHelpers.fixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnlockBetweenWindowTest {

    private static final ObjectMapper MAPPER = Jackson.sharedObjectMapper();

    private final UnlockBetweenWindow window = ImmutableUnlockBetweenWindow.builder()
        .addDays(DayOfWeek.MONDAY)
        .addDays(DayOfWeek.TUESDAY)
        .start(new LocalTime(9,0))
        .end(new LocalTime(17,0))
        .timezone(DateTimeZone.forID("Europe/London"))
        .addExceptions(new LocalDate(2017,5,6))
        .build();

    @Test
    public void serializesToJSON() throws Exception {
        assertEquals(fixture("fixtures/unlock-between-window.json"), MAPPER.writeValueAsString(window));
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        assertEquals(window, MAPPER.readValue(fixture("fixtures/unlock-between-window.json"), UnlockBetweenWindow.class));
    }

}
