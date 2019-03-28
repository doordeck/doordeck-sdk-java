package com.doordeck.sdk.dto.certificate;

import com.doordeck.sdk.jackson.deserializer.DERCertificateDeserializer;
import com.doordeck.sdk.jackson.serializer.DERCertificateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

@Value.Immutable
@JsonDeserialize(as = ImmutableCertificateChain.class)
public interface CertificateChain {

    @JsonDeserialize(contentUsing = DERCertificateDeserializer.class)
    @JsonSerialize(contentUsing = DERCertificateSerializer.class)
    List<X509Certificate> certificateChain();

    UUID userId();

}
