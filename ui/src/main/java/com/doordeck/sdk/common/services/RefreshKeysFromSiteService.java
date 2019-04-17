package com.doordeck.sdk.common.services;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.doordeck.sdk.BuildConfig;
import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;
import com.doordeck.sdk.common.network.RequestQueue;
import com.doordeck.sdk.common.utils.PreferencesManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

public class RefreshKeysFromSiteService implements ThreadedService {

    private final String TAG = RefreshKeysFromSiteService.class.getName();

    private final Executor executor;
    private final MainThread mainThread;
    private int counter = 0;
    private int pos;
    private Callback callback;
    private String siteId;


    public RefreshKeysFromSiteService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void refreshKeys(String siteId, Callback callback) {
        this.callback = callback;
        this.siteId = siteId;
        this.executor.run(this);
    }

    @Override
    public void run() {


        //Instantiate params

        counter++;
        final int position = pos;
        String URL = BuildConfig.BASE_URL_API + "/site/" + siteId +"/device";
        JSONObject emptyParam = new JSONObject();
        //data-binary


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        notifyefreshKeysSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof com.android.volley.NoConnectionError || error instanceof  com.android.volley.TimeoutError) {
                            notifyError(500,"noConnection");
                        }
                        else {

                            String json = null;
                            NetworkResponse response = error.networkResponse;
                            if (response != null && response.data != null) {
                                String result = new String(response.data);
                                try {
                                    JSONObject resp = new JSONObject(result);
//                                    String status = response.getString("status");
                                    String message = resp.getString("message");

//                                    Log.e("Error Status", status);
                                    Log.e("Error Message", message);

                                    notifyError(response.statusCode, message);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }) {//here before semicolon ; and use { }.
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
            public String getBodyContentType() {
                return "application/json";
            }
        };

        // Access the RequestQueue through your singleton class.
        RequestQueue.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    private void notifyError(final int statusCode,final String error) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(statusCode,error);
            }
        });
    }

    private void notifyefreshKeysSuccess(final JSONArray response) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onRefreshKeys(response);
            }
        });
    }

    public interface Callback {
        void onRefreshKeys(JSONArray response);
        void onError(int statusCode, String error);
    }
}

