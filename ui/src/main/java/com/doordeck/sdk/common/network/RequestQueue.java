package com.doordeck.sdk.common.network;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.doordeck.sdk.common.contstants.Constants;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;


public class RequestQueue {

    private static RequestQueue mInstance;

    private com.android.volley.RequestQueue mRequestQueue;
    private static Context mCtx;

    private RequestQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        try {
            SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            ProviderInstaller.installIfNeeded(context.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public static synchronized RequestQueue getInstance() {
        if (mInstance == null) {
            throw new NullPointerException(String.valueOf("RequestQueue is not initialised"));
        }
        return mInstance;
    }

    public static void initialize(Context context) {
        if (mInstance == null) {
            mInstance = new RequestQueue(context);
        }
    }

    public com.android.volley.RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.

            // FIXME pass in Custom SSL Context to HurlStack
            HurlStack hurlStack = new HurlStack() {
                @Override
                public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders)
                        throws IOException, AuthFailureError {
                    Map<String, String> moreHeaders = new HashMap<>(additionalHeaders);
                    moreHeaders.put("Origin", Constants.urls.ORIGIN_API);
                    return super.executeRequest(request, moreHeaders);
                }
            };

            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), hurlStack);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


}

