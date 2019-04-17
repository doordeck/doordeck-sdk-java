package com.doordeck.sdk.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class DERCertificateSerializer extends StdSerializer<X509Certificate> {

    public DERCertificateSerializer() {
        super(X509Certificate.class);
    }

    @Override
    public void serialize(X509Certificate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        try {
            byte[] derBytes = value.getEncoded();
            gen.writeBinary(derBytes);
        } catch (CertificateEncodingException e) {
            throw new IOException(e);
        }
    }
}
