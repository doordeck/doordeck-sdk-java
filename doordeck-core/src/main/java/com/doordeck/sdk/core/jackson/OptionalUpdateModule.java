package com.doordeck.sdk.core.jackson;

import com.doordeck.sdk.core.jackson.deserializer.OptionalUpdateDeserializer;
import com.doordeck.sdk.core.jackson.serializer.OptionalUpdateSerializer;
import com.doordeck.sdk.core.util.OptionalUpdate;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;

import java.lang.reflect.Type;


public class OptionalUpdateModule extends Module {

    @Override
    public void setupModule(Module.SetupContext context) {
        context.addSerializers(new OptionalUpdateSerializers());
        context.addDeserializers(new OptionalUpdateDeserializers());
        // And to fully support OptionalUpdate's, need to modify type info:
        context.addTypeModifier(new OptionalUpdateTypeModifier());
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public String getModuleName() {
        return "OptionalUpdateModule";
    }

    static class OptionalUpdateSerializers extends Serializers.Base {

        @Override
        public JsonSerializer<?> findReferenceSerializer(SerializationConfig config,
                                                         ReferenceType refType, BeanDescription beanDesc,
                                                         TypeSerializer contentTypeSerializer, JsonSerializer<Object> contentValueSerializer)
        {
            final Class<?> raw = refType.getRawClass();
            if (OptionalUpdate.class.isAssignableFrom(raw)) {
                return new OptionalUpdateSerializer(refType, contentTypeSerializer, contentValueSerializer);
            }
            return null;
        }
    }


    static class OptionalUpdateDeserializers extends Deserializers.Base {

        @Override // since 2.7
        public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType,
                                                             DeserializationConfig config, BeanDescription beanDesc,
                                                             TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer)
        {
            if (refType.hasRawClass(OptionalUpdate.class)) {
                return new OptionalUpdateDeserializer(refType, contentTypeDeserializer,contentDeserializer);
            }

            return null;
        }

    }

    static class OptionalUpdateTypeModifier extends TypeModifier {
        @Override
        public JavaType modifyType(JavaType type, Type jdkType, TypeBindings bindings, TypeFactory typeFactory)
        {
            if (type.isReferenceType() || type.isContainerType()) {
                return type;
            }
            final Class<?> raw = type.getRawClass();

            JavaType refType;

            if (raw == OptionalUpdate.class) {
                refType = type.containedTypeOrUnknown(0);
            } else {
                return type;
            }
            return ReferenceType.upgradeFrom(type, refType);
        }
    }

}
