package com.doordeck.sdk.http.interceptor;

import com.google.common.net.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        if (userAgent != null) {
            return chain.proceed(chain.request()
                    .newBuilder()
                    .removeHeader(HttpHeaders.USER_AGENT)
                    .header(HttpHeaders.USER_AGENT, userAgent)
                    .build());
        }
        return chain.proceed(chain.request());
    }
}
