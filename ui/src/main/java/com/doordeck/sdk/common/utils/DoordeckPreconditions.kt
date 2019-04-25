package com.doordeck.sdk.common.utils

import android.app.Activity
import android.content.Context

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller

import java.security.NoSuchAlgorithmException

import javax.net.ssl.SSLContext

internal object DoordeckPreconditions {


    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param <T>          the type parameter
     * @param reference    the reference
     * @param errorMessage the error message
     * @return the t
    </T> */
    fun <T> checkNotNull(reference: T?, errorMessage: Any): T {
        if (reference == null) {
            throw NullPointerException(errorMessage.toString())
        }
        return reference
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression   the expression
     * @param errorMessage the error message
     */
    fun checkArgument(expression: Boolean, errorMessage: Any) {
        if (!expression) {
            throw IllegalArgumentException(errorMessage.toString())
        }
    }

    fun checkTLS(context: Context) {
        try {
            SSLContext.getInstance("TLSv1.2")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        try {
            checkGooglePlayServices(context)
            ProviderInstaller.installIfNeeded(context.applicationContext)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }

    }

    private fun checkGooglePlayServices(context: Context) {
        when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
            ConnectionResult.SERVICE_MISSING -> GoogleApiAvailability.getInstance().getErrorDialog(context as Activity, ConnectionResult.SERVICE_MISSING, 0).show()
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> GoogleApiAvailability.getInstance().getErrorDialog(context as Activity, ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, 0).show()
            ConnectionResult.SERVICE_DISABLED -> GoogleApiAvailability.getInstance().getErrorDialog(context as Activity, ConnectionResult.SERVICE_DISABLED, 0).show()
        }
    }
}
