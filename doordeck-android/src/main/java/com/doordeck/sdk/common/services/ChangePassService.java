package com.doordeck.sdk.common.services;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

/**
 * Created by Gregory on 05/05/2017.
 */

public class ChangePassService implements ThreadedService {

    private static final String TAG = ChangePassService.class.getName();
    private final Executor executor;
    private final MainThread mainThread;

    private String oldPassword;
    private String newPassword;
    private String lockId;
    private Callback callback;

    public ChangePassService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void changePassword(String oldPassword, String newPassword, Callback callback) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
//        this.lockId = lockId;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        //Instantiate params
        String URL = BuildConfig.BASE_URL_API + "/account/password";
        //data-binary

        JSONObject passwordBody = new JSONObject();
        try {
            passwordBody.put("oldPassword", oldPassword);
            passwordBody.put("newPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JSONObject finalPasswordBody = passwordBody;
        StringRequest jsObjRequest = new StringRequest
                (Request.Method.POST, URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG,"lock updated");
                        notifyPasswordChanged();
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
                return finalPasswordBody.toString().getBytes();
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

    private void notifyPasswordChanged() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onPasswordChanged();
            }
        });
    }

    public interface Callback {
        void onPasswordChanged();
        void onError(int statusCode);
    }
}
