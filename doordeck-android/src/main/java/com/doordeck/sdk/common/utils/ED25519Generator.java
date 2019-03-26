package com.doordeck.sdk.common.utils;

import com.doordeck.sdk.jwt.JOSEException;
import com.google.crypto.tink.subtle.Base64;
import com.google.crypto.tink.subtle.Ed25519Sign;

import java.security.GeneralSecurityException;

public class ED25519Generator {

    public static String generatePublicKey() {

        final Ed25519Sign.KeyPair tinkKeyPair;

        try {
            tinkKeyPair = Ed25519Sign.KeyPair.newKeyPair();

        } catch (GeneralSecurityException e) {
            // internal Tink error, should not happen
            throw new JOSEException(e);
        }

        return Base64.encode(tinkKeyPair.getPublicKey());
    }

}
