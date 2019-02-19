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


import com.doordeck.sdk.core.comms.ImmutableRequestDefinition;
import com.doordeck.sdk.core.comms.PluggableHttpClient;
import com.doordeck.sdk.core.comms.ResponseDefinition;
import com.doordeck.sdk.core.util.ManifestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;

import static java.util.Objects.requireNonNull;

public class BaseClient {

    private static final URI DEFAULT_BASE_URL = URI.create("https://api.doordeck.com");

    protected final PluggableHttpClient httpClient;
    protected final ObjectMapper objectMapper;

    private final Optional<URI> origin;
    private final URI baseUrl;

    public BaseClient(PluggableHttpClient httpClient, @Nullable URI baseUrl, @Nullable URI origin, ObjectMapper objectMapper) {
        this.baseUrl = Optional.fromNullable(baseUrl).or(DEFAULT_BASE_URL);
        this.origin = Optional.fromNullable(origin);
        this.httpClient = requireNonNull(httpClient);
        this.objectMapper = requireNonNull(objectMapper);
    }

    protected ImmutableRequestDefinition.Builder requestBuilder(String path) {
        ImmutableRequestDefinition.Builder requestDefinition = ImmutableRequestDefinition.builder()
                .baseUrl(baseUrl)
                .origin(origin)
                .path(path);

        return requestDefinition;
    }

    protected <T> ListenableFuture<T> deserialize(final ListenableFuture<ResponseDefinition> response,
                                                  final TypeReference<T> typeReference) {
        AsyncFunction<ResponseDefinition, T> deserializer =
                new AsyncFunction<ResponseDefinition, T>() {
                    public ListenableFuture<T> apply(ResponseDefinition responseDefinition) {
                        final SettableFuture<T> settableFuture = SettableFuture.create();
                        try {
                            settableFuture.set((T)objectMapper.readValue(responseDefinition.entity(), typeReference));
                        } catch (IOException e) {
                            settableFuture.setException(e);
                        }
                        return settableFuture;
                    }
                };
        return Futures.transformAsync(response, deserializer, MoreExecutors.directExecutor());
    }

    protected <T> ListenableFuture<T> deserialize(final ListenableFuture<ResponseDefinition> response,
                                                  final Class<T> type) {
        AsyncFunction<ResponseDefinition, T> deserializer =
                new AsyncFunction<ResponseDefinition, T>() {
                    public ListenableFuture<T> apply(ResponseDefinition responseDefinition) {
                        final SettableFuture<T> settableFuture = SettableFuture.create();
                        try {
                            settableFuture.set(objectMapper.readValue(responseDefinition.entity(), type));
                        } catch (IOException e) {
                            settableFuture.setException(e);
                        }
                        return settableFuture;
                    }
                };
        return Futures.transformAsync(response, deserializer, MoreExecutors.directExecutor());
    }

}
