/*
 * Copyright 2019 Doordeck Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doordeck.sdk.core.comms.http.client;

import com.doordeck.sdk.core.comms.*;
import com.doordeck.sdk.core.dto.device.Device;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

public class DeviceClient extends BaseClient {

    private static final TypeReference<Set<Device>> DEVICE_SET_TYPE = new TypeReference<Set<Device>>() {};

    public DeviceClient(PluggableHttpClient httpClient, @Nullable URI baseUrl,
                        @Nullable URI origin, ObjectMapper objectMapper) {
        super(httpClient, baseUrl, origin, objectMapper);
    }

    public void getDevices(String authToken, UUID siteId, FutureCallback<Set<Device>> callback) {
        RequestDefinition requestDefinition = requestBuilder("/device/" + siteId.toString())
                .authToken(authToken)
                .responseContentType(MediaType.JSON)
                .method(RequestMethod.GET)
                .build();
        ListenableFuture<ResponseDefinition> response = httpClient.executeCall(requestDefinition);
        Futures.addCallback(deserialize(response, DEVICE_SET_TYPE), callback, MoreExecutors.directExecutor());
    }

}
