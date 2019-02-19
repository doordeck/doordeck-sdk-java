package com.doordeck.sdk.core.comms.http.client;

import com.doordeck.sdk.core.comms.*;
import com.doordeck.sdk.core.dto.site.Site;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Set;

public class SiteClient extends BaseClient {

    private static final TypeReference<Set<Site>> SITE_SET_TYPE = new TypeReference<Set<Site>>() {};

    public SiteClient(PluggableHttpClient httpClient, @Nullable URI baseUrl,
                      @Nullable URI origin, ObjectMapper objectMapper) {
        super(httpClient, baseUrl, origin, objectMapper);
    }

    public void getSites(String authToken, FutureCallback<Set<Site>> callback) {
        RequestDefinition requestDefinition = requestBuilder("/site/")
                .authToken(authToken)
                .responseContentType(MediaType.JSON)
                .method(RequestMethod.GET)
                .build();
        ListenableFuture<ResponseDefinition> response = httpClient.executeCall(requestDefinition);
        Futures.addCallback(deserialize(response, SITE_SET_TYPE), callback, MoreExecutors.directExecutor());
    }
}
