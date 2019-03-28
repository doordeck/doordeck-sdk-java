package com.doordeck.sdk.http.service;

import com.doordeck.sdk.dto.device.Device;
import com.doordeck.sdk.dto.device.ShareableDevice;
import com.doordeck.sdk.dto.device.UpdateDeviceRequest;
import com.doordeck.sdk.dto.operation.Operation;
import com.google.common.net.HttpHeaders;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.UUID;

public interface DeviceService {

    @GET("tile/{tileId}")
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
    Call<Void> executeOperation(@Path("deviceId") UUID deviceId, @Body String signedOperation);
}


