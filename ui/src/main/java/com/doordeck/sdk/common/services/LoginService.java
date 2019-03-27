package com.doordeck.sdk.common.services;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.doordeck.sdk.BuildConfig;
import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;
import com.doordeck.sdk.common.utils.PreferencesManager;
import com.doordeck.sdk.common.network.RequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Gregory on 13/04/2017.
 */



public class LoginService implements ThreadedService {

    private final Executor executor;
    private final MainThread mainThread;

    private Callback callback;
    private String email;
    private String password;


    public LoginService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void login( String email,String password, Callback callback){
        this.email = email;
        this.password = password;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        String URL = BuildConfig.BASE_URL_API + "/auth/token/";

        JSONObject user = new JSONObject();
        try {
            user.put("email", email);
            user.put("password", password);
        }catch (JSONException e){
            e.printStackTrace();
        }
        //data-binary


        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, URL, user, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        saveUser(response);
                        notifyloginSucces();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error instanceof com.android.volley.NoConnectionError) {
                            notifyError(500,"noConnection");

                        }
                        else {
                            notifyError(error.networkResponse.statusCode, "");
                        }


                    }
                });

// Access the RequestQueue through your singleton class.
        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);
    }




    public void saveUser(JSONObject response){
        PreferencesManager.getInstance().put(response);
    }

    private void notifyError(final int statuscode,final String message) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onError(statuscode,message);
            }
        });
    }

    private void notifyloginSucces() {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onLoginSuccess();
            }
        });
    }

    public interface Callback {
        void onLoginSuccess();
        void onError(int statusCode, String message);
    }
}
