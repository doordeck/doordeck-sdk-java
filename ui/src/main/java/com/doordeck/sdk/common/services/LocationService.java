package com.doordeck.sdk.common.services;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.doordeck.ui.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class LocationService {

    private static final int GEO_LOCATION_PERMISSION = 101;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private long UPDATE_INTERVAL = 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 200; /* 2 sec */
    private int NUM_REQ = 1; /* 2 sec */

    private int accuracy;
    private Activity activity;
    private FusedLocationProviderClient mFusedLocationClient;

    public Callback callback;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    public LocationService(Activity activity) {
        this.activity = activity;
    }

    public void getLocation(final int accuracy, Callback callback) {
        this.callback = callback;
        this.accuracy = accuracy;

        //check gps permissions
        if (checkPlayServicesAvailable()) {

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // device has permission, set up request
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
                setupLocationCallback();
                setupLocationRequest();

                // check if location is enabled
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
                SettingsClient client = LocationServices.getSettingsClient(activity);
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

                // location is enabled
                task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    }
                });

                // location is disabled
                task.addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException) {
                            // LocationObject settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(activity,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                                sendEx.printStackTrace();
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });

            } else {
                // request location permission
                requestPermission();
            }

        }

    }

    private void setupLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    callback.onGetLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy());
                    mFusedLocationClient.removeLocationUpdates(this);
                }
            }

//            @Override
//            public void onLocationAvailability(LocationAvailability locationAvailability) {
//
//            }

        };
    }

    private void setupLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(getPriority());
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setNumUpdates(NUM_REQ);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    private int getPriority() {
        switch (accuracy) {
//            case 5:
//                return LocationRequest.PRIORITY_HIGH_ACCURACY;
//            case 10:
//                return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
//            case 50:
//                return LocationRequest.PRIORITY_LOW_POWER;
//            case 200:
//                return LocationRequest.PRIORITY_NO_POWER;
            default:
                return LocationRequest.PRIORITY_HIGH_ACCURACY;
        }
    }

    private boolean checkPlayServicesAvailable() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        switch (resultCode) {
            case ConnectionResult.SUCCESS:
                return true;
            default:
                showPlayServicesError(resultCode);
                return false;
        }
    }

    private void showPlayServicesError(int errorCode) {
        GoogleApiAvailability.getInstance().showErrorDialogFragment(activity, errorCode, 10,
                dialogInterface -> activity.finish());
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(activity, activity.getString(R.string.GEOFENCE_PERMISSION), Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GEO_LOCATION_PERMISSION);
        }
    }

    public boolean inGeofence(double latitude, double longitude, int accuracy, int radius, double lat, double lng, float acc) {
        double distance = distance(latitude, lat, longitude, lng, 0, 0);
        return distance < (radius + accuracy + acc);
    }

    private double distance(double lat1, double lat2, double lon1,
                            double lon2, double el1, double el2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }


    public interface Callback {
        void onGetLocation(double lat, double lng, float acc);

        void onError(String error);
    }
}