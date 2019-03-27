package com.doordeck.sdk.core.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.security.PublicKey;

public class PublicKeySerializer extends JsonSerializer<PublicKey> {


    @Override
    public void serialize(PublicKey value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeBinary(value.getEncoded());
    }

}
