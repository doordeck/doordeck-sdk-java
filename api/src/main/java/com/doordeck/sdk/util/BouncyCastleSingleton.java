package com.doordeck.sdk.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class BouncyCastleSingleton {

    private BouncyCastleSingleton() { /* static class */ }

    private static BouncyCastleProvider provider;

    public static BouncyCastleProvider getInstance() {
        if (provider == null) {
            synchronized (BouncyCastleSingleton.class) {
                if (provider == null) {
                    BouncyCastleSingleton.provider = new BouncyCastleProvider();
                    Security.addProvider(provider);
                }
            }
        }

        return provider;
    }

}
