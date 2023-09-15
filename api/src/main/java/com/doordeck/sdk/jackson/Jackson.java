/*
 * Copyright 2019 Doordeck Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doordeck.sdk.jackson;

import com.doordeck.sdk.jackson.deserializer.DurationDeserializer;
import com.doordeck.sdk.jackson.deserializer.Ed25519PublicKeyDeserializer;
import com.doordeck.sdk.jackson.deserializer.PrivateKeyDeserializer;
import com.doordeck.sdk.jackson.serializer.DurationSerializer;
import com.doordeck.sdk.jackson.serializer.PrivateKeySerializer;
import com.doordeck.sdk.jackson.serializer.PublicKeySerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.joda.time.Duration;

import java.security.PrivateKey;
import java.security.PublicKey;

public class Jackson {

    private Jackson() { /* static class */ }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new OptionalUpdateModule())
            .registerModule(new GuavaModule())
            .registerModule(new JodaModule())
            .registerModule(new SimpleModule()
                    .addSerializer(new DurationSerializer())
                    .addDeserializer(Duration.class, new DurationDeserializer())
                    .addSerializer(PublicKey.class, new PublicKeySerializer())
            .addSerializer(PrivateKey.class, new PrivateKeySerializer())
            .addDeserializer(PublicKey.class, new Ed25519PublicKeyDeserializer())
            .addDeserializer(PrivateKey.class, new PrivateKeyDeserializer())
            )
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static ObjectMapper sharedObjectMapper() {
        return OBJECT_MAPPER;
    }

}
