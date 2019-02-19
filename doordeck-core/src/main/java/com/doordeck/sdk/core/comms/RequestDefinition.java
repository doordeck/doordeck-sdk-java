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

package com.doordeck.sdk.core.comms;

import com.doordeck.sdk.core.util.ManifestUtils;
import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import org.immutables.value.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Value.Immutable
public abstract class RequestDefinition {

    private static final String DEFAULT_USER_AGENT = "Doordeck SDK - " + ManifestUtils.getManfiestVersion().or("Unknown");

    public abstract URI baseUrl();
    public abstract String path();
    public abstract Multimap<String, Object> queryParameters();
    public abstract RequestMethod method();
    public abstract Optional<URI> origin();
    public abstract Optional<String> request();
    public abstract Optional<Class> responseType();
    public abstract Optional<String> authToken();

    @Value.Default
    public String userAgent() {
        return DEFAULT_USER_AGENT;
    }

    @Value.Default
    public MediaType requestContentType() {
        return MediaType.JSON;
    }

    @Value.Default
    public MediaType responseContentType() {
        return MediaType.JSON;
    }

    @Value.Derived
    public URI endpoint() {
        // Flatten query parameters into string
        StringBuilder queryBuilder = new StringBuilder();
        for (Map.Entry<String, Object> queryParam : queryParameters().entries()) {
            queryBuilder.append(String.format("%s=%s", queryParam.getKey(), queryParam.getValue().toString()));
            queryBuilder.append('&');
        }
        // Strip final delimiter
        if (queryBuilder.length() > 0) {
            queryBuilder.deleteCharAt(queryBuilder.length());
        }

        String query = queryBuilder.length() == 0 ? null : queryBuilder.toString();

        try {
            return new URI(baseUrl().getScheme(), baseUrl().getUserInfo(), baseUrl().getHost(), baseUrl().getPort(), path(), query, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
