package com.doordeck.sdk.jackson.serializer;

import com.doordeck.sdk.util.DayOfWeek;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class AmericanDaySeralizer extends StdSerializer<DayOfWeek> {

    public AmericanDaySeralizer() {
        super(DayOfWeek.class);
    }

    @Override
    public void serialize(DayOfWeek value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int dayOfWeek = DayOfWeek.SUNDAY.equals(value) ? 0 : value.getValue();
        gen.writeNumber(dayOfWeek);
    }
}
