package com.doordeck.sdk.dto.device;

import static com.doordeck.sdk.util.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;

import com.doordeck.sdk.jackson.Jackson;
import com.doordeck.sdk.util.DayOfWeek;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public class UnlockBetweenWindowTest {

    private static final ObjectMapper MAPPER = Jackson.sharedObjectMapper();

    private final UnlockBetweenWindow window = ImmutableUnlockBetweenWindow.builder()
        .addDays(DayOfWeek.MONDAY)
        .addDays(DayOfWeek.TUESDAY)
        .start(LocalTime.of(9,0))
        .end(LocalTime.of(17,0))
        .timezone(ZoneId.of("Europe/London"))
        .addExceptions(LocalDate.of(2017,5,6))
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
