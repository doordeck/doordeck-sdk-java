package com.doordeck.sdk.http;

import com.doordeck.sdk.dto.device.Device;

import org.junit.Test;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

public class DoordeckClientTest {

    private String BASE_URL_API = "https://api.dev.doordeck.com";
    private String USER_AGENT_PREFIX = "Doordeck SDK - ";
    private String APIKEY = "eyJraWQiOiJkZWZhdWx0IiwiYWxnIjoiRVMyNTYifQ.eyJzdWIiOiJjNWRlNWVmMC03MTFiLTExZTctOTgyMy1hOWY3MzZkYWM3NjYiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwic2Vzc2lvbiI6IjU0ZjdkOGQwLWEyM2ItMTFlOS1iMTg3LWY1MGNmZjIwZDU3NyIsIm5hbWUiOiJHcmVnb3J5IFBlZXRlcnMiLCJpc3MiOiJodHRwczpcL1wvYXBpLmRvb3JkZWNrLmNvbVwvIiwicmVmcmVzaCI6ZmFsc2UsImV4cCI6MTU2Mjc1NzUzMiwiaWF0IjoxNTYyNjcxMTMyLCJlbWFpbCI6ImdyZWdvcnlAZG9vcmRlY2suY29tIiwic2lkIjoiNTRmN2Q4ZDAtYTIzYi0xMWU5LWIxODctZjUwY2ZmMjBkNTc3In0.w88UIroi05l_qXR3nSKOKKXNJtr3suU265RKoNfEUyq1_uRZIhc4DaK9pfyJz3ZHIASjVth3WcBt1oP4ugZS6Q";

    @Test
    public void requestTile() {
        long startTime = System.currentTimeMillis();
        DoordeckClient client = new DoordeckClient.Builder()
                .baseUrl(URI.create(BASE_URL_API))
                .userAgent(USER_AGENT_PREFIX + 1)
                .authToken(APIKEY)
                .build();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("ClientSetup: " + elapsedTime);
        CompletableFuture<String> future = new CompletableFuture<>();
        client.device().resolveTile(UUID.fromString("f1192b4c-21cc-45b4-bd67-43dad9856f58")).enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                System.out.println("TileRequestSuccess: " + elapsedTime);
                future.complete("SUCCESS");
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                System.out.println(t.toString());
                System.out.println("TileRequestFail: " + elapsedTime);
                future.complete("FAIL");
            }
        });
        try {
            assertEquals("SUCCESS", future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
