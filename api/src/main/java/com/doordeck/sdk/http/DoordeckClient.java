package com.doordeck.sdk.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class DoordeckClient {

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

    private final OkHttpClient okHttp;

    private DoordeckClient() {
        this.okHttp = new OkHttpClient.Builder()
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

    public static class Builder {

        private String userAgent;
        private URI origin;
        private ObjectMapper objectMapper;

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        @Deprecated
        public Builder origin(URI origin) {
            this.origin = origin;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public DoordeckClient build() {
            return new DoordeckClient();
        }

    }

}
