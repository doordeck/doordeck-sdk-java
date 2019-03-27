package com.doordeck.sdk.ui.unlock;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import com.doordeck.sdk.common.contstants.Constants;
import com.doordeck.sdk.common.executor.MainThreadImpl;
import com.doordeck.sdk.common.executor.ThreadExecutor;
import com.doordeck.sdk.common.models.Lock;
import com.doordeck.sdk.common.network.APINetworkError;
import com.doordeck.sdk.common.services.GetKeyIdFromTileService;
import com.doordeck.sdk.common.services.LocationService;
import com.doordeck.sdk.common.services.UnlockService;
import com.doordeck.sdk.common.utils.NdefReaderTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class UnlockPresenter {

    private final LocationService locationService;
    private UnlockView view;
    private MainThreadImpl mainThread;
    private ThreadExecutor executor;
    private final String TAG = "UNLOCK_PRESENTER";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private String lockName;
    private Lock lockToUnlock;

    public UnlockPresenter(UnlockView view) {
        this.view = view;
        this.mainThread = new MainThreadImpl();
        this.executor = new ThreadExecutor();
        locationService = new LocationService((Activity) view);
    }

    public void handleIntent(Intent intent) {
        if (intent.getStringExtra(Constants.intent.QR_UNLOCK) != null) {
            String id = intent.getStringExtra(Constants.intent.QR_UNLOCK);
            unlockFromTileId(id);
//            setUserIsEducated();
        } else if (intent.getStringExtra(Constants.intent.WIDGET_UNLOCK) != null) {
            try {
                JSONObject lock = new JSONObject(intent.getStringExtra(Constants.intent.WIDGET_UNLOCK));
//                mNfcPresenter.checkSettings(lock);
//                setUserIsEducated();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (intent.getStringExtra(Constants.intent.BUTTON_UNLOCK) != null) {
            try {
                JSONObject lock = new JSONObject(intent.getStringExtra(Constants.intent.BUTTON_UNLOCK));
                unlock(lock);
//                mNfcPresenter.checkSettings(lock);
//                mUnlockViaBtn = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
//            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
            handleNFCIntent(intent);
//            setUserIsEducated();
        }
    }

    public void handleNFCIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(executor, mainThread).execute(tag, new NdefReaderTask.Callback() {
                    @Override
                    public void onReadSuccess(String tileId) {
                        unlockFromTileId(tileId);
                    }

                    @Override
                    public void onError(String message) {
                        if(view.isActive()){
                        }
                    }
                });

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask(executor, mainThread).execute(tag, new NdefReaderTask.Callback() {
                        @Override
                        public void onReadSuccess(String tileId) {
                            unlockFromTileId(tileId);
                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                    break;
                }
            }
        }
    }

    public void unlockFromTileId(String tileId) {
        new GetKeyIdFromTileService(executor, mainThread).getLock(tileId, new GetKeyIdFromTileService.Callback() {
            @Override
            public void onLockReceived(String lockId) {
                try {
                    JSONObject lock = new JSONObject(lockId);
                    unlock(lock);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(APINetworkError error) {
                switch (error.errorStatus) {
                    default:
                        if(view.isActive()){
                            view.showAccessDenied();
                        }
                }
            }
        });
    }

//    private JSONObject getSavedLock(String lockID) {
//        if (PreferencesManager.getInstance().has(Constants.prefs.SAVED_LOCKS)){
//            try {
//                JSONArray savedLocks = new JSONArray(PreferencesManager.getInstance().getString(Constants.prefs.SAVED_LOCKS));
//                for (int i = 0, l = savedLocks.length(); i < l; i++) {
//                    JSONObject lock = savedLocks.getJSONObject(i);
//                    if(lock.get(Constants.lock.ID).equals(lockID)) {
//                        return lock;
//                    }
//                }
//                return null;
//            } catch (JSONException e) {
//                e.printStackTrace();
//                return null;
//            }
//        } else return null;
//    }

    public void unlock(JSONObject lock) {
        try {
            lockToUnlock = new Lock(lock);
            if(view.isActive()) {
                view.updateLockName(lockToUnlock.name);
                view.setUnlocking();
            }

            if (lock.getJSONObject("settings").getJSONObject("usageRequirements").has("location")) {
                if (lock.getJSONObject("settings").getJSONObject("usageRequirements").getJSONObject("location").optBoolean("enabled", false)) {
                    if (view.isActive()) {
                        view.showGeoLoading();
                    }
                    lockToUnlock.setGeofence(lock.getJSONObject("settings").getJSONObject("usageRequirements").getJSONObject("location"));
                    if (view.isActive()) {
                        view.checkGoogleApiPermissions();
                    }

                } else {
                    doUnlock();
                }
            } else {
                doUnlock();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void doUnlock() {
        new UnlockService(executor, mainThread).Unlock(lockToUnlock.id, new UnlockService.Callback() {
            @Override
            public void onUnlock(long success) {
                if(view.isActive()){
                    view.unlockSuccess();
                }
            }

            @Override
            public void onError(String error) {
                if(view.isActive()){
                    view.showAccessDenied();
                }
            }
        });
    }

    public void checkGeofence() {

        locationService.getLocation(lockToUnlock.accuracy, new LocationService.Callback() {
            @Override
            public void onGetLocation(double lat, double lng, float acc) {
                if (locationService.inGeofence(lockToUnlock.lat, lockToUnlock.lng, lockToUnlock.accuracy, lockToUnlock.radius, lat, lng, acc)) {
                    doUnlock();
                } else {
                    if(view.isActive()){
                        if(view.isActive()) view.showNoAccessGeoFence();
                    }
                }

            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void setFinishTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (view.isActive()){
                    view.finishActivity();
                }
            }
        },5000);
    }
}
