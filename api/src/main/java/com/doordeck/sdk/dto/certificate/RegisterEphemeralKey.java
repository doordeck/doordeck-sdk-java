package com.doordeck.sdk.dto.certificate;

import com.doordeck.sdk.jackson.deserializer.Ed25519PublicKeyDeserializer;
import com.doordeck.sdk.jackson.serializer.PublicKeySerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.security.PublicKey;

@Value.Immutable
@JsonSerialize(as = ImmutableRegisterEphemeralKey.class)
@JsonDeserialize(as = ImmutableRegisterEphemeralKey.class)
public interface RegisterEphemeralKey {

    @JsonSerialize(using = PublicKeySerializer.class)
    @JsonDeserialize(using = Ed25519PublicKeyDeserializer.class)
    PublicKey ephemeralKey();

}
