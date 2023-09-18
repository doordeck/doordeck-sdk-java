package com.doordeck.sdk.dto.device;

import com.doordeck.sdk.dto.Role;
import com.doordeck.sdk.jackson.Jackson;
import com.doordeck.sdk.util.DayOfWeek;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.util.UUID;

import static com.doordeck.sdk.util.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;

public class DeviceTest {

    private static final ObjectMapper MAPPER = Jackson.sharedObjectMapper();

    private final Device complexDevice = ImmutableDevice.builder()
            .deviceId(UUID.fromString("ddb74c90-7c1e-11e7-9823-a9f736dac766"))
            .name("Paxton Net2 (1787501)")
            .favourite(false)
            .start(Instant.parse("2018-05-11T08:15:00.000Z"))
            .end(Instant.parse("2018-05-18T16:30:00.000Z"))
            .role(Role.ADMIN)
            .colour("#24BD9A")
            .settings(ImmutableDeviceSetting.builder()
                    .defaultName("Paxton Net2 (1787501)")
                    .addTile(UUID.fromString("ddb74c90-7c1e-11e7-9823-a9f736dac766"))
                    .addTile(UUID.fromString("baef898d-4c07-4546-91c8-3d03f5dd4702"))
                    .usageRequirements(ImmutableDeviceUsageRequirements.builder()
                            .location(ImmutableLocationRequirement.builder()
                                    .latitude(51.52196559999999)
                                    .longitude(-0.08481929999993554)
                                    .radius(50)
                                    .accuracy(20)
                                    .enabled(false)
                                    .build())
                            .addTime(ImmutableTimeRequirement.builder()
                                    .addDays(DayOfWeek.WEDNESDAY)
                                    .addDays(DayOfWeek.FRIDAY)
                                    .start(LocalTime.parse("08:00"))
                                    .end(LocalTime.parse("18:00"))
                                    .timezone(DateTimeZone.forID("Europe/London"))
                                    .build())
                            .build())
                    .unlockBetween(ImmutableUnlockBetweenWindow.builder()
                            .addDays(DayOfWeek.TUESDAY)
                            .addDays(DayOfWeek.WEDNESDAY)
                            .start(LocalTime.parse("08:00"))
                            .end(LocalTime.parse("18:00"))
                            .timezone(DateTimeZone.forID("Europe/London"))
                            .addExceptions(LocalDate.parse("2019-03-29"))
                            .build())
                    .build())
            .build();

    private final Device simpleDevice = ImmutableDevice.builder()
            .deviceId(UUID.fromString("ddb74c90-7c1e-11e7-9823-a9f736dac766"))
            .name("Paxton Net2 (1787501)")
            .favourite(false)
            .role(Role.USER)
            .colour("#24BD9A")
            .settings(ImmutableDeviceSetting.builder()
                    .defaultName("Paxton Net2 (1787501)")
                    .build())
            .build();

    @Test
    public void deserializeComplexFromJSON() throws Exception {
        assertEquals(complexDevice, MAPPER.readValue(fixture("fixtures/complex-device.json"), Device.class));
    }

    @Test
    public void deserializeSimpleFromJSON() throws Exception {
        assertEquals(simpleDevice, MAPPER.readValue(fixture("fixtures/simple-device.json"), Device.class));
    }

}
