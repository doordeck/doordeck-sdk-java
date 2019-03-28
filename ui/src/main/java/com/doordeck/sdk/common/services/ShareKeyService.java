package com.doordeck.sdk.common.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.doordeck.sdk.BuildConfig;
import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;
import com.doordeck.sdk.common.network.RequestQueue;
import com.doordeck.sdk.common.utils.Helper;
import com.doordeck.sdk.common.utils.JWTUtil;
import com.doordeck.sdk.common.utils.PreferencesManager;
import com.doordeck.sdk.dto.ImmutableAddUserOperation;
import com.doordeck.sdk.dto.ImmutableMutateDoorState;
import com.doordeck.sdk.dto.Operation;
import com.google.common.base.Optional;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;
import static com.doordeck.sdk.common.utils.DoordeckPreconditions.getValueOrDefault;

public class ShareKeyService implements ThreadedService {

    private final Executor executor;
    private final MainThread mainThread;

    private String mLock_id;
    private Callback callback;

    private Context context;
    private String email;
    private Instant start;
    private Instant end;
    private float exp;

    public ShareKeyService(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void shareLock(String lock_id, String email, Instant start, Instant end, float exp, Callback callback) {
        this.mLock_id = lock_id;
        this.callback = callback;
        this.email = email;
        this.executor.run(this);
        this.start = start;
        this.end = end;
        this.exp = exp;
    }

    @Override
    public void run() {

        requestUserDataFromEmail();
    }

    private void requestUserDataFromEmail() {
        //Get userId from email
        //https://api.doordeck.com/share/invite/USER_EMAIL
        final String mEmail = email;
        String URL = BuildConfig.BASE_URL_API + "/share/invite/" + email + "/" + mLock_id ;

        JSONObject emptyParam = new JSONObject();
        //data-binary

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, URL, emptyParam, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("lock=",response.toString());
                        try {
                            String id = response.getString("id");
                            String publicKey = response.getString("publicKey");

                            UserData userData = new UserData(id, publicKey);

                            requestShareLock(userData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String json = null;

                        NetworkResponse response = error.networkResponse;
                        if (error instanceof com.android.volley.NoConnectionError) {
                            notifyError(mLock_id, mEmail, 500);
                        }
                        else {
                            if (response != null && response.data != null) {
                                notifyError( mLock_id, mEmail,response.statusCode);
                            }
                        }
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
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
// Access the RequestQueue through your singleton class.
        RequestQueue.getInstance().addToRequestQueue(jsObjRequest);
    }

    private void requestShareLock(UserData userData) {

        //Instantiate params
        String deviceID = mLock_id;
        String URL = BuildConfig.BASE_URL_API + "/device/"+deviceID+"/execute";
        final String mEmail = email;
        String signedJWT = null;



        Operation op = ImmutableAddUserOperation.builder()
                .user(UUID.fromString(userData.getId()))
                .publicKey(Helper.getKey(userData.getPublicKey()))
                .start(Optional.fromNullable(start))
                .end(Optional.fromNullable(end))
                .build();

        try {
            signedJWT = JWTUtil.signJWT(deviceID, Instant.now().plus(Duration.standardSeconds(60)), op);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }


        final String finalSignedJWT = signedJWT;

        Log.v("ShareLock", deviceID.toString());

        StringRequest jsStringRequest = new StringRequest
                (Request.Method.POST,URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        notifySharedSuccess(mLock_id,mEmail);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String message = null;

                        NetworkResponse response = error.networkResponse;

                        if (error instanceof com.android.volley.NoConnectionError) {
                            notifyError(mLock_id, mEmail, 500);
                        }
                        else if (error instanceof com.android.volley.TimeoutError) {
                            notifyError(mLock_id, mEmail, 409);
                        }
                        else {
                            if (response != null && response.data != null) {
                                notifyError( mLock_id, mEmail,response.statusCode);
                            }
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
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    if(finalSignedJWT != null){
                        return finalSignedJWT.getBytes("utf-8");
                    }
                    return null;
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", finalSignedJWT, "utf-8");
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        // Access the RequestQueue through your singleton class.
        jsStringRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue.getInstance().addToRequestQueue(jsStringRequest);
    }

    private void notifyError(final String id, final String email, final int statuscCde) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(id,email,statuscCde);
            }
        });
    }

    private void notifySharedSuccess(final String lockId, final String email) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onSharedSuccess(lockId,email);
            }
        });
    }

    public interface Callback {
        void onSharedSuccess(String id, String email);
        void onError(String id, String email, int statusCode);
    }

    private class UserData{
        private String id;
        private String publicKey;

        public UserData(String id, String publicKey) {
            this.id = id;
            this.publicKey = publicKey;
        }

        public String getId() {
            return id;
        }

        public String getPublicKey() {
            return publicKey;
        }
    }
}
