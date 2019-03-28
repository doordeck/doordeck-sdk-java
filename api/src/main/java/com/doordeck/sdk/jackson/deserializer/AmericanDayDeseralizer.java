package com.doordeck.sdk.jackson.deserializer;

import com.doordeck.sdk.util.DayOfWeek;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Represents DayOfWeek as integer starting from Sunday as 0
 *
 * Sunday = 0 (6 in Java)
 * Monday = 1 (1 in Java)
 * Tuesday = 2 (2 in Java)
 * etc
 */
public class AmericanDayDeseralizer extends StdDeserializer<DayOfWeek> {

    public AmericanDayDeseralizer() {
        super(DayOfWeek.class);
    }

    @Override
    public DayOfWeek deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        int dayOfWeek = p.getIntValue();
        return dayOfWeek == 0 ? DayOfWeek.SUNDAY : DayOfWeek.values()[dayOfWeek];
    }

}
