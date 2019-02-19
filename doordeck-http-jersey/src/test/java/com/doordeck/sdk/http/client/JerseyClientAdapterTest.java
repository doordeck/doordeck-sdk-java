package com.doordeck.sdk.http.client;

import com.doordeck.sdk.core.comms.PluggableHttpClient;
import com.doordeck.sdk.core.comms.http.client.DeviceClient;
import com.doordeck.sdk.core.dto.device.Device;
import com.doordeck.sdk.core.jackson.Jackson;
import com.google.common.util.concurrent.FutureCallback;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JerseyClientAdapterTest {

    String authToken = System.getenv("DOORDECK_AUTH_TOKEN");

    @Test
    public void testGetDevices() throws Exception {
        PluggableHttpClient httpClient = new JerseyClientAdapter();
        DeviceClient deviceClient = new DeviceClient(httpClient, null, null, Jackson.sharedObjectMapper());

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        FutureCallback<Set<Device>> deviceCallback = new FutureCallback<Set<Device>>() {
            @Override
            public void onSuccess(@NullableDecl Set<Device> devices) {
                System.out.println(devices);
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println(t.getMessage());
                countDownLatch.countDown();
            }
        };

        deviceClient.getDevices(authToken, UUID.fromString("7659e430-4a28-11e8-bf0b-bffab372a82e"), deviceCallback);

        // Prevent test from exiting until threads done
        countDownLatch.await(30, TimeUnit.SECONDS);
    }

}
