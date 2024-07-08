package com.doordeck.sdk.dto.operation;

import static com.doordeck.sdk.util.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;

import com.doordeck.sdk.dto.device.ImmutableUnlockBetweenWindow;
import com.doordeck.sdk.dto.device.UnlockBetweenWindow;
import com.doordeck.sdk.jackson.Jackson;
import com.doordeck.sdk.util.DayOfWeek;
import com.doordeck.sdk.util.OptionalUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

public class MutateSettingTest {

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
        MutateSetting setting = ImmutableMutateSetting.builder()
            .unlockDuration(Duration.ofSeconds(7))
            .unlockBetween(OptionalUpdate.update(window))
            .build();
        assertEquals(fixture("fixtures/settings-with-window-and-duration.json"), MAPPER.writeValueAsString(setting));
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        MutateSetting setting = ImmutableMutateSetting.builder()
            .unlockDuration(Duration.ofSeconds(7))
            .unlockBetween(OptionalUpdate.update(window))
            .build();
        assertEquals(setting, MAPPER.readValue(fixture("fixtures/settings-with-window-and-duration.json"), Operation.class));
    }

    @Test
    public void testDeserializeNullValue() throws Exception {
        Operation op = MAPPER.readValue(fixture("fixtures/settings-with-null-window.json"), Operation.class);
        assertEquals(true, op instanceof MutateSetting);
        assertEquals(OptionalUpdate.delete(), ((MutateSetting)op).unlockBetween());
        assertEquals(Optional.empty(), ((MutateSetting)op).unlockDuration());
    }

    @Test
    public void testSerializeNullValue() throws Exception {
        MutateSetting setting = ImmutableMutateSetting.builder()
            .unlockBetween(OptionalUpdate.<UnlockBetweenWindow>delete())
            .build();
        Operation op = MAPPER.readValue(fixture("fixtures/settings-with-null-window.json"), Operation.class);
        assertEquals(setting, op);
    }

    @Test
    public void testDeserializeUnspecifiedValue() throws Exception {
        Operation op = MAPPER.readValue(fixture("fixtures/settings-without-values.json"), Operation.class);
        assertEquals(true, op instanceof MutateSetting);
        assertEquals(OptionalUpdate.preserve(), ((MutateSetting)op).unlockBetween());
        assertEquals(Optional.empty(), ((MutateSetting)op).unlockDuration());
    }

    @Test
    public void testSerializeUnspecifiedValue() throws Exception {
        MutateSetting setting = ImmutableMutateSetting.builder()
            .unlockBetween(OptionalUpdate.<UnlockBetweenWindow>preserve())
            .build();
        Operation op = MAPPER.readValue(fixture("fixtures/settings-without-values.json"), Operation.class);
        assertEquals(setting, op);
    }

}
