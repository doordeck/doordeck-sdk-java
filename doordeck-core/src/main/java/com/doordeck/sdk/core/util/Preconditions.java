package com.doordeck.sdk.core.util;

public class Preconditions {

    private Preconditions() { /* Static class */ }

    public static void checkArgument(boolean conditionToPass, String message) {
        if (!conditionToPass) {
            throw new IllegalArgumentException(message);
        }
    }

}
