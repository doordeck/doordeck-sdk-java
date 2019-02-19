package com.doordeck.sdk.http.client;

import com.doordeck.sdk.core.comms.ImmutableResponseDefinition;
import com.doordeck.sdk.core.comms.PluggableHttpClient;
import com.doordeck.sdk.core.comms.RequestDefinition;
import com.doordeck.sdk.core.comms.ResponseDefinition;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.io.InputStream;

public class JerseyClientAdapter implements PluggableHttpClient {

    private final Client client;

    public JerseyClientAdapter() {
        this.client = JerseyClientBuilder.createClient();
    }

    @Override
    public ListenableFuture<ResponseDefinition> executeCall(RequestDefinition requestDefinition) {

        Invocation.Builder builder = client.target(requestDefinition.endpoint())
                .request()
                .header(HttpHeaders.USER_AGENT, requestDefinition.userAgent())
                .accept(requestDefinition.responseContentType().getMimeType());

        // Add origin
        if (requestDefinition.origin().isPresent()) {
            builder.header(HttpHeaders.ORIGIN, requestDefinition.origin().get());
        }

        // Add authentication
        if (requestDefinition.authToken().isPresent()) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + requestDefinition.authToken().get());
        }

        // Setup future callback
        final SettableFuture<ResponseDefinition> responseFuture = SettableFuture.create();
        InvocationCallback<Response> invocationCallback = new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                    responseFuture.set(ImmutableResponseDefinition.builder()
                            .statusCode(response.getStatus())
                            .entity(response.readEntity(InputStream.class))
                            .build());
                } else {
                    responseFuture.setException(new IllegalStateException(response.getStatusInfo().toString()));
                }
            }

            @Override
            public void failed(Throwable throwable) {
                responseFuture.setException(throwable);
            }
        };

        // Send the request
        if (requestDefinition.request().isPresent()) {
            Entity entity = Entity.entity(requestDefinition.request().orNull(), requestDefinition.requestContentType().getMimeType());
            builder.async().method(requestDefinition.method().name(), entity, invocationCallback);
        } else {
            builder.async().method(requestDefinition.method().name(), invocationCallback);
        }

        return responseFuture;
    }

}
