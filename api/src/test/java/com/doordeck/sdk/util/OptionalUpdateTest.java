package com.doordeck.sdk.util;

import com.doordeck.sdk.jackson.Jackson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OptionalUpdateTest {

    private static final ObjectMapper OBJECT_MAPPER = Jackson.sharedObjectMapper();

    @Test
    public void testPreserveSerialization() throws Exception {
        TestClass test = TestClass.of(OptionalUpdate.<String>preserve());
        String actualJson = OBJECT_MAPPER.writeValueAsString(test);
        assertEquals("{}", actualJson);
    }

    @Test
    public void testUpdateSerialization() throws Exception {
        TestClass test = TestClass.of(OptionalUpdate.update("hello"));
        String actualJson = OBJECT_MAPPER.writeValueAsString(test);
        assertEquals("{\"value\":\"hello\"}", actualJson);
    }

    @Test
    public void testDeleteSerialization() throws Exception {
        TestClass test = TestClass.of(OptionalUpdate.<String>delete());
        String actualJson = OBJECT_MAPPER.writeValueAsString(test);
        assertEquals("{\"value\":null}", actualJson);
    }

    @Test
    public void testPreserveDeserialization() throws Exception {
        String json = "{}";
        TestClass actual = OBJECT_MAPPER.readValue(json, TestClass.class);
        TestClass expected = TestClass.of(OptionalUpdate.<String>preserve());
        assertEquals(expected, actual);
    }

    @Test
    public void testUpdateDeserialization() throws Exception {
        String json = "{\"value\":\"hello\"}";
        TestClass actual = OBJECT_MAPPER.readValue(json, TestClass.class);
        TestClass expected = TestClass.of(OptionalUpdate.update("hello"));
        assertEquals(expected, actual);
    }

    @Test
    public void testDeleteDeserialization() throws Exception {
        String json = "{\"value\":null}";
        TestClass actual = OBJECT_MAPPER.readValue(json, TestClass.class);
        TestClass expected = TestClass.of(OptionalUpdate.<String>delete());
        assertEquals(expected, actual);
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableTestClass.class)
    @JsonDeserialize(as = ImmutableTestClass.class)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static abstract class TestClass {

        @Value.Default
        OptionalUpdate<String> value() {
            return OptionalUpdate.preserve();
        }

        static TestClass of(OptionalUpdate<String> value) {
            return ImmutableTestClass.builder().value(value).build();
        }

    }


}
