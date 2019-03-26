package com.doordeck.sdk.common.services;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.doordeck.sdk.BuildConfig;
import com.doordeck.sdk.common.contstants.Constants;
import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;
import com.doordeck.sdk.common.network.RequestQueue;
import com.doordeck.sdk.common.utils.PreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

/**
 * Created by Gregory on 05/05/2017.
 */

public class ChangeDisplayNameService implements ThreadedService {

    private static final String TAG = ChangeDisplayNameService.class.getName();
    private final Executor executor;
    private final MainThread mainThread;

    private String name;
    private Callback callback;

    public ChangeDisplayNameService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void changeName(String name, Callback callback) {
        this.name = name;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        //Instantiate params
        String URL = BuildConfig.BASE_URL_API + "/account";
        //data-binary

        JSONObject userBody = new JSONObject();
        try {
            userBody.put(Constants.user.DISPLAY_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JSONObject finalUserBody = userBody;
        StringRequest jsObjRequest = new StringRequest
                (Request.Method.POST, URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG,"lock updated");
                        notifyNameChanged();
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
                return finalUserBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        // Access the RequestQueue through your singleton class.
        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);

    }


    private void notifyError(final int statusCode) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(statusCode);
            }
        });
    }

    private void notifyNameChanged() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onNameChanged();
            }
        });
    }

    public interface Callback {
        void onNameChanged();
        void onError(int statusCode);
    }
}
