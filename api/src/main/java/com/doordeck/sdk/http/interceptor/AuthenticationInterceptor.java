package com.doordeck.sdk.http.interceptor;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Adds the auth token to every request
 */
public class AuthenticationInterceptor implements Interceptor {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final Supplier<String> authTokenSupplier;
    private volatile String authToken;

    public AuthenticationInterceptor(Supplier<String> authTokenSupplier) {
        this.authTokenSupplier = authTokenSupplier;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request()
                .newBuilder()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + getAuthToken())
                .build());
    }

    private String getAuthToken() {
        // FIXME check expiry time of auth token

        if (authToken == null) {
            synchronized (AuthenticationInterceptor.class) {
                if (authToken == null) {
                    this.authToken = Objects.requireNonNull(authTokenSupplier.get());
                }
            }
        }

        return authToken;
    }

}
