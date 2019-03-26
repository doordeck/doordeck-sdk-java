package com.doordeck.sdk.core.dto;

import com.doordeck.sdk.core.jackson.Jackson;
import com.doordeck.sdk.core.util.DayOfWeek;
import com.doordeck.sdk.core.util.OptionalUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import static com.doordeck.sdk.core.util.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;

public class MutateSettingTest {

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
        MutateSetting setting = ImmutableMutateSetting.builder()
            .unlockDuration(Duration.standardSeconds(7))
            .unlockBetween(OptionalUpdate.update(window))
            .build();
        assertEquals(fixture("fixtures/settings-with-window-and-duration.json"), MAPPER.writeValueAsString(setting));
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        MutateSetting setting = ImmutableMutateSetting.builder()
            .unlockDuration(Duration.standardSeconds(7))
            .unlockBetween(OptionalUpdate.update(window))
            .build();
        assertEquals(setting, MAPPER.readValue(fixture("fixtures/settings-with-window-and-duration.json"), Operation.class));
    }

    @Test
    public void testDeserializeNullValue() throws Exception {
        Operation op = MAPPER.readValue(fixture("fixtures/settings-with-null-window.json"), Operation.class);
        assertEquals(true, op instanceof MutateSetting);
        assertEquals(OptionalUpdate.delete(), ((MutateSetting)op).unlockBetween());
        assertEquals(Optional.absent(), ((MutateSetting)op).unlockDuration());
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
        assertEquals(Optional.absent(), ((MutateSetting)op).unlockDuration());
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
