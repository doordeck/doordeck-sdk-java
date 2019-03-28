package com.doordeck.sdk.http.service;

import com.doordeck.sdk.dto.Device;
import com.google.common.net.HttpHeaders;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

import java.util.List;
import java.util.UUID;

public interface DeviceService {

    @GET("tile/{tileId}")
    @Headers(HttpHeaders.ACCEPT + ": application/vnd.doordeck.api-v2+json, application/json") // Prefer newer endpoint
    Call<Device> resolveTile(@Header(HttpHeaders.AUTHORIZATION) String authCode, @Path("tileId") UUID tileId);

    @GET("device/{deviceId}")
    @Headers(HttpHeaders.ACCEPT + ": application/vnd.doordeck.api-v2+json") // Only accept v2
    Call<Device> getDevice(@Header(HttpHeaders.AUTHORIZATION) String authCode, @Path("deviceId") UUID deviceId);

    @GET("site/{siteId}/device")
    Call<List<Device>> getSiteDevices(@Header(HttpHeaders.AUTHORIZATION) String authCode, @Path("siteId") UUID siteId);

}


