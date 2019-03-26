package com.doordeck.sdk.common.services;


import android.util.Log;

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
import com.doordeck.sdk.common.network.APINetworkError;
import com.doordeck.sdk.common.network.RequestQueue;
import com.doordeck.sdk.common.utils.PreferencesManager;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

public class GetKeyService implements ThreadedService {

    private final Executor executor;
    private final MainThread mainThread;

    private String mLock_id;
    private Callback callback;

    public GetKeyService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void getLock(String lock_id, Callback callback) {
        this.mLock_id = lock_id;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        //Instantiate params

        String URL = BuildConfig.BASE_URL_API + "/device/"+mLock_id;
        JSONObject emptyParam = new JSONObject();
        //data-binary

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, URL, emptyParam, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("lock=",response.toString());
                        notifylock(response);
//                      mTxtDisplay.setText("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String json = null;

                        NetworkResponse response = error.networkResponse;
                        notifyError(new APINetworkError("test", response.statusCode));

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
        };
        //jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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

    private void notifylock(final JSONObject lockJson) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onLockLoaded(lockJson);
            }
        });
    }

    public interface Callback {
        void onLockLoaded(final JSONObject lockJson);
        void onError(APINetworkError error);
    }
}
