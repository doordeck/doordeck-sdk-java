package com.doordeck.sdk.common.services;

import android.util.Log;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.doordeck.sdk.BuildConfig;
import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;
import com.doordeck.sdk.common.network.RequestQueue;
import com.doordeck.sdk.common.utils.PreferencesManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.REFRESHTOKEN;
import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

/**
 * Created by Gregory on 15/05/2017.
 */

public class RefreshTokenService implements ThreadedService {

    private final Executor executor;
    private final MainThread mainThread;

    private Callback callback;


    public RefreshTokenService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void refreshToken(Callback callback) {
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {


        //Instantiate params

        String URL = BuildConfig.BASE_URL_API + "/auth/token/refresh";
        JSONObject emptyParam = new JSONObject();
        //data-binary

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, URL, emptyParam, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        saveUser(response);
                        notifyRefreshTokenSuccess();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof com.android.volley.NoConnectionError) {
                            notifyError(500);
                        }
                        else {
                            String json = null;

                            NetworkResponse response = error.networkResponse;
                            notifyError(response.statusCode);
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String rToken = getValueOrDefault(PreferencesManager.getInstance().getString(REFRESHTOKEN),"");
                String bearer = "Bearer ".concat(rToken);
                Map<String, String> headersSys = super.getHeaders();
                Map<String, String> headers = new HashMap<>();
                headersSys.remove("Authorization");
                headers.put("Authorization", bearer);
                headers.putAll(headersSys);
                return headers;


            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);
    }

    private void saveUser(JSONObject response){
        PreferencesManager.getInstance().put(response);

    }

    private void notifyError(final int error) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onError(error);
            }
        });
    }

    private void notifyRefreshTokenSuccess() {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onRefreshToken();
            }
        });
    }

    public interface Callback {
        void onRefreshToken();
        void onError(int error);
    }
}
