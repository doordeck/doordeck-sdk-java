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

package com.doordeck.sdk.core.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Jackson {

    private Jackson() { /* static class */ }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new OptionalUpdateModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

    public static ObjectMapper sharedObjectMapper() {
        return OBJECT_MAPPER;
    }

}
