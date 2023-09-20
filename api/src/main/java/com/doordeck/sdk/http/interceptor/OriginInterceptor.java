package com.doordeck.sdk.http.interceptor;

import java.io.IOException;
import java.net.URI;

import javax.annotation.Nullable;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OriginInterceptor implements Interceptor {

    private final URI origin;

    public OriginInterceptor(@Nullable URI origin) {
        this.origin = origin;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();

        if (origin != null) {
            return chain.proceed(originalRequest
                    .newBuilder()
                    .addHeader("Origin", origin.toString())
                    .build());
        } else {
            return chain.proceed(originalRequest);
        }
    }

}
