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

package com.doordeck.sdk.signer.util;

import com.google.common.io.BaseEncoding;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class CertUtils {

    private CertUtils() { /* Static class */ }

    public static List<String> asBase64(List<X509Certificate> certificates) throws CertificateEncodingException {
        BaseEncoding encoder = BaseEncoding.base64Url().omitPadding();

        List<String> b64EncodedCertificates = new ArrayList<>(certificates.size());
        for (X509Certificate certificate : certificates) {
            b64EncodedCertificates.add(encoder.encode(certificate.getEncoded()));
        }
        return b64EncodedCertificates;
    }

}
