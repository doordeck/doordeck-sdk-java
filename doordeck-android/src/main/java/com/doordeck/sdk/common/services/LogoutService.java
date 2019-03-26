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
import com.doordeck.sdk.common.utils.Helper;
import com.doordeck.sdk.common.utils.PreferencesManager;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;

public class LogoutService implements ThreadedService {

    private final String TAG = LogoutService.class.getName();

    private final Executor executor;
    private final MainThread mainThread;

    private Callback callback;


    public LogoutService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void logout(Callback callback) {
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        String URL = BuildConfig.BASE_URL_API + "/auth/token/destroy";
        JSONObject emptyParam = new JSONObject();
        //data-binary
//        Answers.getInstance().logCustom(new CustomEvent(FabricEvents.LOGOUT));

        StringRequest jsObjRequest = new StringRequest
                (Request.Method.POST, URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        //--MainActivity.getController().onLogoutSuccess();
//                        Answers.getInstance().logCustom(new CustomEvent(FabricEvents.LOGOUT_SUCCESS));

                        notifyLogoutSuccess();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String json = null;

                        NetworkResponse response = error.networkResponse;
                        if (response != null && response.data != null) {

//                            notifyError(json);

                            //Additional cases

                        }

//                        Log.v(TAG, error.toString());

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                String Token = PreferencesManager.getInstance().getString(TOKEN);
                String bearer = "Bearer ".concat(Token);
                Map<String, String> headersSys = super.getHeaders();
                Map<String, String> headers = new HashMap<>();
                headersSys.remove("Authorization");
                headers.put("Authorization", bearer);
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.putAll(headersSys);
                return headers;


            }
        };

        // Access the RequestQueue through your singleton class.
        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);
    }

    private void notifyError(final String error) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onError(error);
            }
        });
    }

    private void notifyLogoutSuccess() {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onLogoutSuccess();
            }
        });
    }

    public interface Callback {
        void onLogoutSuccess();
        void onError(String error);
    }
}
