package com.doordeck.sdk.common.services;


import com.android.volley.AuthFailureError;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationService implements ThreadedService {

    private final Executor executor;
    private final MainThread mainThread;

    private Callback callback;
    private String userName;
    private String email;
    private String password;

    public RegistrationService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void registration(String userName, String email, String password, Callback callback){
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {

        String URL = BuildConfig.BASE_URL_API + "/auth/register/";

        JSONObject user = new JSONObject();
        try {
            user.put("displayName", userName);
            user.put("email", email);
            user.put("password", password);
        }catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, URL, user, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        saveUser(response);
                        notifyRegistered();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof com.android.volley.NoConnectionError) {
                            notifyError(500,"noConnection");

                        }
                        else {
                            notifyError(error.networkResponse.statusCode,"");
                        }
                    }
                }){//here before semicolon ; and use { }.
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headersSys = super.getHeaders();
                Map<String, String> headers = new HashMap<>();
                headersSys.remove("Accept");
                headers.put("Accept","application/vnd.doordeck.api-v2+json");
                headers.putAll(headersSys);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);
    }

    public void saveUser(JSONObject response){
        PreferencesManager.getInstance().put(response);

    }
    private void notifyError(final int statusCode, final String message) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(statusCode, message);
            }
        });
    }

    private void notifyRegistered() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onRegistrationSuccess();
            }
        });
    }

    public interface Callback {
        void onRegistrationSuccess();
        void onError(int statusCode, String message);
    }
}

