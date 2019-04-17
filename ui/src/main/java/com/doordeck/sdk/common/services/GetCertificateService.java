package com.doordeck.sdk.common.services;

import com.android.volley.AuthFailureError;
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
import com.doordeck.sdk.jwt.JOSEException;
import com.doordeck.sdk.signer.Ed25519KeyGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

/**
 * Created by Gregory on 13/04/2017.
 */



public class GetCertificateService implements ThreadedService {

    private final Executor executor;
    private final MainThread mainThread;

    private Callback callback;

    public GetCertificateService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void getCertificate(Callback callback){
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        String URL = BuildConfig.BASE_URL_API + "/auth/certificate/";
//        String URL = BuildConfig.BASE_URL_API;

        final JSONObject pkey = new JSONObject();
        try {
            pkey.put(Constants.prefs.EPHEMERAL_KEY, Ed25519KeyGenerator.generate());
        }catch (GeneralSecurityException | JSONException e){
            e.printStackTrace();
        }
        //data-binary



        StringRequest jsObjRequest = new StringRequest
                (Request.Method.POST, URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        notifyCertificateSuccess(response);
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
                }){
            @Override
            public byte[] getBody() throws AuthFailureError {
                return pkey.toString().getBytes();
            }
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
        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);
    }


    private void notifyError(final int statuscode, final String message) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onError(statuscode,message);
            }
        });
    }

    private void notifyCertificateSuccess(final String cert) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onCertificateSuccess(cert);
            }
        });
    }

    public interface Callback {
        void onCertificateSuccess(String cert);
        void onError(int statusCode, String message);
    }
}
