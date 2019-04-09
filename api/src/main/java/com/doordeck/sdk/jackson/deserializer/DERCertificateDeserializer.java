package com.doordeck.sdk.jackson.deserializer;

import com.doordeck.sdk.util.BouncyCastleSingleton;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class DERCertificateDeserializer extends StdDeserializer<X509Certificate> {

    private static CertificateFactory CERTIFICATE_FACTORY;

    public DERCertificateDeserializer() {
        super(X509Certificate.class);

        try {
            if (CERTIFICATE_FACTORY == null) {
                synchronized (DERCertificateDeserializer.class) {
                    if (CERTIFICATE_FACTORY == null) {
                        CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509", BouncyCastleSingleton.getInstance());
                    }
                }
            }
        } catch (CertificateException e) {
            throw new IllegalStateException("Unable to setup certificate factory");
        }
    }

    @Override
    public X509Certificate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        byte[] encodedCert = p.getBinaryValue();

        try (ByteArrayInputStream certStream = new ByteArrayInputStream(encodedCert)) {
            Certificate certificate = CERTIFICATE_FACTORY.generateCertificate(certStream);

            if (certificate instanceof X509Certificate) {
                return (X509Certificate)certificate;
            }

            throw new IOException("Could not convert certificate to X509 certificate");
        } catch (CertificateException e) {
            throw new IOException(e);
        }

    }


}
