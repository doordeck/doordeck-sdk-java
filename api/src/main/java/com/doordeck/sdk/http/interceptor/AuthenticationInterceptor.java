package com.doordeck.sdk.http.interceptor;

import com.google.common.net.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Simply adds the 'Bearer' prefix to auth tokens
 */
public class AuthenticationInterceptor implements Interceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String originalAuthCode = originalRequest.header(HttpHeaders.AUTHORIZATION);

        if (originalAuthCode != null && !originalAuthCode.startsWith(BEARER_PREFIX)) {
            return chain.proceed(originalRequest
                    .newBuilder()
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + originalAuthCode)
                    .build());
        } else {
            return chain.proceed(originalRequest);
        }
    }

}
