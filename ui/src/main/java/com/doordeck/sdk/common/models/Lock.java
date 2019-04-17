package com.doordeck.sdk.common.models;

import com.doordeck.sdk.common.contstants.Constants;

import org.json.JSONException;
import org.json.JSONObject;


public class Lock {

    public String name;
    public String id;
    public String color;
    public Boolean favourite;
    public int unlockTime;
    public double lat;
    public double lng;
    public int radius;
    public int accuracy;

    public Lock(JSONObject lock) {

        try {
            this.name = lock.getString(Constants.lock.NAME);
            this.id = lock.getString(Constants.lock.ID);
            this.unlockTime = lock.getJSONObject(Constants.lock.SETTINGS).getInt(Constants.lock.UNLOCK_TIME);
            this.color = lock.getString(Constants.lock.COLOR);
            this.favourite = lock.getBoolean(Constants.lock.FAVOURITE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setGeofence(JSONObject geofence) {
        this.lat = geofence.optDouble(Constants.lock.LATITUDE, 0);
        this.lng = geofence.optDouble(Constants.lock.LONGITUDE, 0);
        this.radius = geofence.optInt(Constants.lock.RADIUS, 0);
        this.accuracy = geofence.optInt(Constants.lock.ACCURACY, 0);
    }
}
