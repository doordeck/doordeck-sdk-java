package com.doordeck.sdk.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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
                        CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509");
                    }
                }
            }
        } catch (CertificateException e) {
            throw new IllegalStateException("Unable to setup certificate factory");
        }
    }

    @Override
    public X509Certificate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try (PipedInputStream pipeIn = new PipedInputStream(); PipedOutputStream pipeOut = new PipedOutputStream()){
            pipeIn.connect(pipeOut);

            p.readBinaryValue(pipeOut);
            Certificate certificate = CERTIFICATE_FACTORY.generateCertificate(pipeIn);

            if (certificate instanceof X509Certificate) {
                return (X509Certificate)certificate;
            }

            throw new IOException("Could not convert certificate to X509 certificate");
        } catch (CertificateException e) {
            throw new IOException(e);
        }

    }


}
