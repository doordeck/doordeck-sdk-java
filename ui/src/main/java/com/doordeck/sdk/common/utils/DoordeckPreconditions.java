package com.doordeck.sdk.common.utils;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class DoordeckPreconditions {



    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param <T>          the type parameter
     * @param reference    the reference
     * @param errorMessage the error message
     * @return the t
     */
    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression   the expression
     * @param errorMessage the error message
     */
    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static void checkGooglePlayServices(Context context){
        switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)){
            case ConnectionResult.SERVICE_MISSING:
                GoogleApiAvailability.getInstance().getErrorDialog((Activity)context,ConnectionResult.SERVICE_MISSING,0).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                GoogleApiAvailability.getInstance().getErrorDialog((Activity)context,ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,0).show();
                break;
            case ConnectionResult.SERVICE_DISABLED:
                GoogleApiAvailability.getInstance().getErrorDialog((Activity)context,ConnectionResult.SERVICE_DISABLED,0).show();
                break;
        }
    }
}
