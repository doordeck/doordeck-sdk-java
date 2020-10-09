package com.doordeck.sdk.http.service;

import com.doordeck.sdk.dto.device.Device;
import com.doordeck.sdk.dto.device.ShareableDevice;
import com.doordeck.sdk.dto.device.UpdateDeviceRequest;
import com.google.common.net.HttpHeaders;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DeviceService {

    @GET("device/{tileId}")
    @Headers(HttpHeaders.ACCEPT + ": application/vnd.doordeck.api-v2+json, application/json") // Prefer newer endpoint
    Call<Device> resolveTile(@Path("tileId") UUID tileId);

    @GET("device/{deviceId}")
    @Headers(HttpHeaders.ACCEPT + ": application/vnd.doordeck.api-v2+json") // Only accept v2
    Call<Device> getDevice(@Path("deviceId") UUID deviceId);

    @GET("site/{siteId}/device")
    Call<List<Device>> getSiteDevices(@Path("siteId") UUID siteId);

    @PUT("device/{deviceId}")
    Call<Void> updateDevice(@Path("deviceId") UUID deviceId, @Body UpdateDeviceRequest updateDeviceRequest);

    @GET("device/favourite")
    Call<List<Device>> getPinnedDevices();

    @GET("device/shareable")
    Call<List<ShareableDevice>> getShareableDevices();

    @POST("device/{deviceId}/execute")
    @Headers(HttpHeaders.CONTENT_TYPE + ": application/jwt")
    Call<Void> executeOperation(@Path("deviceId") UUID deviceId, @Body String signedOperation);
}


