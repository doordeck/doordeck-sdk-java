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
import com.doordeck.sdk.jackson.serializer.DurationSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.Duration;

public class Jackson {

    private Jackson() { /* static class */ }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new OptionalUpdateModule())
            .registerModule(new GuavaModule())
            .registerModule(new JodaModule())
            .registerModule(new SimpleModule()
                    .addSerializer(new DurationSerializer())
                    .addDeserializer(Duration.class, new DurationDeserializer())
            )
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static ObjectMapper sharedObjectMapper() {
        return OBJECT_MAPPER;
    }

}
