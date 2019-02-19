package com.doordeck.sdk.http.client;

import com.doordeck.sdk.core.comms.ImmutableResponseDefinition;
import com.doordeck.sdk.core.comms.PluggableHttpClient;
import com.doordeck.sdk.core.comms.RequestDefinition;
import com.doordeck.sdk.core.comms.ResponseDefinition;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class OkHttpClientAdapter implements PluggableHttpClient {

    private static final String[] TRUSTED_CERTIFICATES = {
            "sha256/++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=",
            "sha256/f0KW/FtqTjs108NpYj42SrGvOB2PpxIVM8nWxjPqJGE=",
            "sha256/NqvDJlas/GRcYbcWE8S/IceH9cq77kg0jVhZeAPXq8k=",
            "sha256/9+ze1cZgR9KO1kZrVDxA4HQ6voHRCSVNz4RdTCx4U8U=",
            "sha256/KwccWaCgrnaw6tsrrSO61FgLacNgG2MMLq8GE6+oP5I=",
            "sha256/tYU57KoTkhzNuA0400h1/eZHHFoVnZvu8vpvmZg71hE=",
            "sha256/ZLtb2AMR+j9TvZlATKuHYq1uBIRH0Kl/IZ/OyhZh83w=",
            "sha256/G9pa//g3gTgL9wgZj599LbHgZ/FLuep7rnCqwLAwXns=",
            "sha256/fFO133kTXZr2GV72u3OrmMLImVC4krGS3/14TbklpBw=",
            "sha256/F3CN/yt/rsnLG1IV67JCHZewVDyTb6ydbgK5LyDlxwc="
    };

    private final OkHttpClient client;

    /**
     * Create an OkHttp client adapter with sensible defaults
     */
    public OkHttpClientAdapter() {
        this.client = new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(ConnectionSpec.RESTRICTED_TLS))
                .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .retryOnConnectionFailure(true)
                .cookieJar(CookieJar.NO_COOKIES) // Disable cookies
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .certificatePinner(new CertificatePinner.Builder()
                        .add("*", TRUSTED_CERTIFICATES)
                        .build())
                .build();
    }

    public OkHttpClientAdapter(OkHttpClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public ListenableFuture<ResponseDefinition> executeCall(RequestDefinition requestDefinition) {

        Request.Builder request = new Request.Builder().url(requestDefinition.endpoint().toString())
                .header(HttpHeaders.USER_AGENT, requestDefinition.userAgent());

        // Process body
        if (requestDefinition.request().isPresent()) {
            MediaType mediaType = MediaType.get(requestDefinition.requestContentType().getMimeType());
            RequestBody requestBody = RequestBody.create(mediaType, requestDefinition.request().get());
            request.method(requestDefinition.method().name(), requestBody);
        } else {
            request.method(requestDefinition.method().name(), null);
        }

        // Add authentication
        if (requestDefinition.authToken().isPresent()) {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + requestDefinition.authToken().get());
        }

        // Add origin
        if (requestDefinition.origin().isPresent()) {
            request.addHeader(HttpHeaders.ORIGIN, requestDefinition.origin().get().toString());
        }

        // Handle response
        final SettableFuture<ResponseDefinition> responseFuture = SettableFuture.create();
        Callback responseCallback = new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                responseFuture.setException(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    responseFuture.set(ImmutableResponseDefinition.builder()
                            .statusCode(response.code())
                            .entity(response.body().byteStream())
                            .build());
                } else {
                    responseFuture.setException(new IOException(response.message()));
                }
            }

        };

        client.newCall(request.build()).enqueue(responseCallback);

        return responseFuture;
    }
}
