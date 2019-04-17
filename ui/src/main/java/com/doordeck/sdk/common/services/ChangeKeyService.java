package com.doordeck.sdk.common.services;


import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.doordeck.sdk.BuildConfig;
import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;
import com.doordeck.sdk.common.network.RequestQueue;
import com.doordeck.sdk.common.utils.PreferencesManager;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

public class ChangeKeyService implements ThreadedService {

    private final String TAG = ChangeKeyService.class.getName();

    private final Executor executor;
    private final MainThread mainThread;

    private Callback callback;

    private String lockId;
    private JSONObject properties;

    public ChangeKeyService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void updateKey(String lockId, final JSONObject properties, Callback callback) {
        this.callback = callback;
        this.lockId = lockId;
        this.properties = properties;
        this.executor.run(this);
    }

    @Override
    public void run() {

        String URL = BuildConfig.BASE_URL_API + "/device/"+lockId;
        //data-binary
        final JSONObject property = properties;
        StringRequest jsObjRequest = new StringRequest
                (Request.Method.PUT, URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.v(TAG,"lock updated");
                        notifyLockUpdated(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        NetworkResponse response = error.networkResponse;
                        notifyError(response.statusCode);

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                String myToken = getValueOrDefault(PreferencesManager.getInstance().getString(TOKEN),"");
                String bearer = "Bearer ".concat(myToken);
                Map<String, String> headersSys = super.getHeaders();
                Map<String, String> headers = new HashMap<>();
                headersSys.remove("Authorization");
                headers.put("Authorization", bearer);
                headers.putAll(headersSys);
                return headers;


            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    Log.v("colorSent=",property.toString());
                    byte[] bytes = property.toString().getBytes("utf-8");
                    return properties == null ? null : bytes;
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", properties, "utf-8");
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        // Access the RequestQueue through your singleton class.
        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);
    }

    private void notifyError(final int error) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(error);
            }
        });
    }

    private void notifyLockUpdated(final String response) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onLockUpdated(response);
            }
        });
    }

    public interface Callback {
        void onLockUpdated(String response);
        void onError(int error);
    }
}

