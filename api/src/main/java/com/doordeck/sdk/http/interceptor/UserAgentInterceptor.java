package com.doordeck.sdk.http.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

    public static final String USER_AGENT_HEADER = "User-Agent";

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        if (userAgent != null) {
            return chain.proceed(chain.request()
                    .newBuilder()
                    .removeHeader(USER_AGENT_HEADER)
                    .header(USER_AGENT_HEADER, userAgent)
                    .build());
        }
        return chain.proceed(chain.request());
    }
}
