package com.doordeck.sdk.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class PreferencesManager {

    private static final String PREF_NAME = "com.doordeck.PREF_NAME";

    private static final String TAG = "PREFS";

    private static PreferencesManager sInstance;
    private final SharedPreferences mPref;

    private PreferencesManager(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(context);
        }
    }

    public static synchronized PreferencesManager getInstance() {
        if (sInstance == null) {
            throw new NullPointerException(String.valueOf("PreferencesManager is not initialised"));
        }
        return sInstance;
    }

    public boolean has(String key) {
        return mPref.contains(key);
    }

    public String getString(String key) {
        return mPref.getString(key, null);
    }

    public void setString(String key, String value) {
        mPref.edit()
                .putString(key, value)
                .commit();
    }

    public int getInt(String key) {
        return mPref.getInt(key, 0);
    }

    public void setInt(String key, int value) {
        mPref.edit()
                .putInt(key, value)
                .commit();
    }

    public void setLong(String key, long value) {
        mPref.edit()
                .putLong(key, value)
                .commit();
    }

    public long getLong(String key) {
        return mPref.getLong(key, 0);
    }

    public void remove(String key) {
        mPref.edit()
                .remove(key)
                .commit();
    }

    public boolean clear() {
        return mPref.edit()
                .clear()
                .commit();
    }

    public void put(JSONObject jsonData) {
        Iterator x = jsonData.keys();

        ;

        while (x.hasNext()){
            String key = (String) x.next();
            String val = null;
            try {
                val = jsonData.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if(!val.equals("null")) {
                    mPref.edit().putString(key, val).commit();
                }
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }

        }

        Log.v(TAG,"Saving preferences");

    }

    public boolean getBoolean(String value) {
        return mPref.getBoolean(value, false);
    }
}
