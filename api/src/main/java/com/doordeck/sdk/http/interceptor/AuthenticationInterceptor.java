package com.doordeck.sdk.http.interceptor;

import com.google.common.base.Supplier;
import com.google.common.net.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adds the auth token to every request
 */
public class AuthenticationInterceptor implements Interceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final Supplier<String> authTokenSupplier;
    private String authToken;

    public AuthenticationInterceptor(Supplier<String> authTokenSupplier) {
        this.authTokenSupplier = authTokenSupplier;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request()
                .newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + getAuthToken())
                .build());
    }

    private String getAuthToken() {
        // FIXME check expiry time of auth token

        if (authToken == null) {
            synchronized (AuthenticationInterceptor.class) {
                if (authToken == null) {
                    this.authToken = checkNotNull(authTokenSupplier.get());
                }
            }
        }

        return authToken;
    }

}
