package com.doordeck.sdk.http.service;

import com.doordeck.sdk.dto.device.Device;
import com.doordeck.sdk.dto.device.MultiDeviceResponse;
import com.doordeck.sdk.dto.device.ShareableDevice;

import java.util.List;
import java.util.UUID;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DeviceService {

    @GET("tile/{tileId}")
    @Headers("Accept: application/vnd.doordeck.api-v3+json") // Prefer newer endpoint
    Call<MultiDeviceResponse> resolveTile(@Path("tileId") UUID tileId);

    @GET("device/{deviceId}")
    @Headers("Accept: application/vnd.doordeck.api-v2+json")
    Call<Device> getDevice(@Path("deviceId") UUID deviceId);

    @GET("site/{siteId}/device")
    Call<List<Device>> getSiteDevices(@Path("siteId") UUID siteId);

    @PUT("device/{deviceId}")
    Call<Void> updateDevice(@Path("deviceId") UUID deviceId, @Body RequestBody updateDeviceRequest);

    @GET("device/favourite")
    Call<List<Device>> getPinnedDevices();

    @GET("device/shareable")
    Call<List<ShareableDevice>> getShareableDevices();

    @POST("device/{deviceId}/execute")
    @Headers("Content-Type: application/jwt")
    Call<Void> executeOperation(@Path("deviceId") UUID deviceId, @Body String signedOperation);
}


