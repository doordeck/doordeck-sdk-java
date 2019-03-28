package com.doordeck.sdk.http.interceptor;

import com.google.common.net.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;

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
                    .addHeader(HttpHeaders.ORIGIN, origin.toString())
                    .build());
        } else {
            return chain.proceed(originalRequest);
        }
    }

}
