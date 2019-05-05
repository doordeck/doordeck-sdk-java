package com.doordeck.sdk.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.security.PrivateKey;

public class PrivateKeySerializer extends JsonSerializer<PrivateKey> {
    public PrivateKeySerializer() {
    }

    @Override
    public void serialize(PrivateKey value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeBinary(value.getEncoded());
    }
}
