package com.doordeck.sdk.http;

import com.doordeck.sdk.http.interceptor.AuthenticationInterceptor;
import com.doordeck.sdk.http.interceptor.OriginInterceptor;
import com.doordeck.sdk.http.interceptor.UserAgentInterceptor;
import com.doordeck.sdk.http.service.CertificateService;
import com.doordeck.sdk.http.service.DeviceService;
import com.doordeck.sdk.http.service.SiteService;
import com.doordeck.sdk.jackson.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class DoordeckClient {

    private static final URI DEFAULT_BASE_URL = URI.create("https://api.doordeck.com");

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
    private final Retrofit retrofit;

    private final DeviceService deviceService;
    private final SiteService siteService;
    private final CertificateService certificateService;

    private DoordeckClient(Builder config) {
        this.okHttp = new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(ConnectionSpec.RESTRICTED_TLS))
                .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .retryOnConnectionFailure(true)
                .cookieJar(CookieJar.NO_COOKIES) // Disable cookies
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(new OriginInterceptor(config.origin))
                .addNetworkInterceptor(new AuthenticationInterceptor(config.authTokenSupplier))
                .addNetworkInterceptor(new UserAgentInterceptor(config.userAgent))
                .followRedirects(true)
                .followSslRedirects(false)
                .certificatePinner(new CertificatePinner.Builder()
                        .add("*", TRUSTED_CERTIFICATES)
                        .build())
                .build();

        this.retrofit = new Retrofit.Builder()
                .client(okHttp)
                .baseUrl(Optional.fromNullable(config.baseUrl).or(DEFAULT_BASE_URL).toString())
                .addConverterFactory(
                        JacksonConverterFactory.create(Optional
                                .fromNullable(config.objectMapper)
                                .or(Jackson.sharedObjectMapper())))
                .build();

        this.deviceService = retrofit.create(DeviceService.class);
        this.certificateService = retrofit.create(CertificateService.class);
        this.siteService = retrofit.create(SiteService.class);
    }

    public DeviceService device() {
        return deviceService;
    }

    public SiteService site() {
        return siteService;
    }

    public CertificateService certificateService() {
        return certificateService;
    }

    public Retrofit retrofit() {
        return retrofit;
    }

    public static class Builder {

        private Supplier<String> authTokenSupplier;
        private String userAgent;
        private URI origin;
        private ObjectMapper objectMapper;
        private URI baseUrl;

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder baseUrl(URI baseUrl) {
            this.baseUrl = baseUrl;
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

        public Builder authToken(Supplier<String> authTokenSupplier) {
            this.authTokenSupplier = authTokenSupplier;
            return this;
        }

        public Builder authToken(String authToken) {
            this.authTokenSupplier = () -> authToken;
            return this;
        }

        public DoordeckClient build() {
            return new DoordeckClient(this);
        }

    }

}
