package com.doordeck.sdk.common.services;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.doordeck.sdk.BuildConfig;
import com.doordeck.sdk.R;
import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;
import com.doordeck.sdk.common.network.APINetworkError;
import com.doordeck.sdk.common.network.RequestQueue;
import com.doordeck.sdk.common.utils.Helper;
import com.doordeck.sdk.common.utils.PreferencesManager;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

public class GetKeyIdFromTileService implements ThreadedService {

    private final Executor executor;
    private final MainThread mainThread;

    private Callback callback;
    private String tileID;


    public GetKeyIdFromTileService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void getLock(String tileID, Callback callback) {
        this.tileID = tileID;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {


        //Instantiate params

        String URL = BuildConfig.BASE_URL_API + "/device/" + this.tileID;
        JSONObject emptyParam = new JSONObject();
        //data-binary

        StringRequest jsObjRequest = new StringRequest
                (Request.Method.GET, URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        notifyLockReceivedSuccess(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if(error instanceof  com.android.volley.ServerError) {
                            if(error.networkResponse.statusCode == 303) {
                                String location = error.networkResponse.headers.get("Location");
                                String id = location.substring(location.lastIndexOf("/") + 1);
                                if(Helper.isUUID(id)){
                                    notifyLockReceivedSuccess(id);
                                } else {
                                    notifyError(new APINetworkError("Could not retrieve lock", error.networkResponse.statusCode));
                                }
                            }
                            else notifyError(new APINetworkError("test", error.networkResponse.statusCode));
                        }

                        else if (error instanceof com.android.volley.NoConnectionError) {
                            notifyError(new APINetworkError("No Connection", error.networkResponse.statusCode));
                        }
                        else {
                            notifyError(new APINetworkError("Error", error.networkResponse.statusCode));
                        }
                    }
                }){
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
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
// Access the RequestQueue through your singleton class.
        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);
    }


    private void notifyError(final APINetworkError error) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onError(error);
            }
        });
    }

    private void notifyLockReceivedSuccess(final String response) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onLockReceived(response);
            }
        });
    }

    public interface Callback {
        void onLockReceived(String lock);
        void onError(APINetworkError error);
    }
}
