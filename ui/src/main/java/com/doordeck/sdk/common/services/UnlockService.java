package com.doordeck.sdk.common.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.doordeck.sdk.BuildConfig;
import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;
import com.doordeck.sdk.common.network.RequestQueue;
import com.doordeck.sdk.common.utils.JWTUtil;
import com.doordeck.sdk.common.utils.PreferencesManager;
import com.doordeck.sdk.dto.operation.ImmutableMutateDoorState;
import com.doordeck.sdk.dto.operation.Operation;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

/**
 * Created by Gregory on 15/05/2017.
 */

public class UnlockService implements ThreadedService {


    private final Executor executor;
    private final MainThread mainThread;

    private Callback callback;

    private Context context;
    private String lockId;
    private long mRequestStartTime;


    public UnlockService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void Unlock(String lockId, Callback callback) {
        this.lockId = lockId;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {

        String mLockId = lockId;
        //Instantiate params
        String deviceID = mLockId;
        String URL = BuildConfig.BASE_URL_API + "/device/" + deviceID + "/execute";
        String signedJWT = null;

        Operation op = ImmutableMutateDoorState.builder().locked(false).build();

        if (PreferencesManager.getInstance().has(TOKEN)) {
            mRequestStartTime = System.currentTimeMillis();
            try {
                signedJWT = JWTUtil.signJWT(deviceID, Instant.now().plus(Duration.standardSeconds(60)), op);
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }

            long totaldecodeTime = System.currentTimeMillis() - mRequestStartTime;
            Log.v("decodeTime = ", String.valueOf(totaldecodeTime));
            mRequestStartTime = System.currentTimeMillis();
            final String finalSignedJWT = signedJWT;


            StringRequest jsStringRequest = new StringRequest
                    (Request.Method.POST, URL, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            long totalRequestTime = System.currentTimeMillis() - mRequestStartTime;
                            Log.v("reqTime = ", String.valueOf(totalRequestTime));

                            notifyUnlockSuccess(totalRequestTime);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            long totalRequestTime = System.currentTimeMillis() - mRequestStartTime;
                            Log.v("reqTime = ", String.valueOf(totalRequestTime));
                            NetworkResponse networkResponse = error.networkResponse;
                            String errorMessage = "Unknown error";
                            if (networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = "Request timeout";
                                } else if (error.getClass().equals(NoConnectionError.class)) {
                                    errorMessage = "Failed to connect server";
                                }
                            } else {
                                String result = new String(networkResponse.data);
                                try {
                                    JSONObject response = new JSONObject(result);
//                                    String status = response.getString("status");
                                    String message = response.getString("message");

//                                    Log.e("Error Status", status);
                                    Log.e("Error Message", message);

                                    notifyError(message);

//                                    switch (networkResponse.statusCode){
//                                        case 400: notifyError(m);
//                                    }
//                                    if (networkResponse.statusCode == 404) {
//                                        notifyError("Resource not found");
//                                    } else if (networkResponse.statusCode == 401) {
//                                        errorMessage = message + " Please login again";
//                                    } else if (networkResponse.statusCode == 400) {
//                                        errorMessage = message + " Check your inputs";
//                                    } else if (networkResponse.statusCode == 500) {
//                                        errorMessage = message + " Something is getting wrong";
//                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.i("Error", errorMessage);
                            error.printStackTrace();

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
                        if(finalSignedJWT != null){
                            byte[] bytes = finalSignedJWT.getBytes(StandardCharsets.UTF_8);
                            return finalSignedJWT == null ? null : bytes;
                        }
                        return null;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            jsStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Access the RequestQueue through your singleton class.
            RequestQueue.getInstance().addToRequestQueue(jsStringRequest);

        }
        else {
            notifyError("noToken");
        }
    }


    private void notifyError(final String error) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onError(error);
            }
        });
    }

    private void notifyUnlockSuccess(final long success) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onUnlock(success);
            }
        });
    }

    public interface Callback {
        void onUnlock(long success);
        void onError(String error);
    }
}
