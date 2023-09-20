package com.doordeck.sdk.dto.operation;

import static com.doordeck.sdk.util.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;

import com.doordeck.sdk.jackson.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

public class OperationTest {

    private static final ObjectMapper MAPPER = Jackson.sharedObjectMapper();

    @Before
    public void before() throws Exception {
        var decodedKey = Base64.getDecoder().decode(PUBLIC_KEY);
        var keySpec = new X509EncodedKeySpec(decodedKey);
        KEY = KeyFactory.getInstance("RSA").generatePublic(keySpec);

        ADD_USER = ImmutableAddUserOperation.builder().user(USER).publicKey(KEY).build();
    }

    UUID USER = UUID.fromString("b7865920-2e2d-11e6-a7d5-6b27bc204923");
    AddUserOperation ADD_USER;
    String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6xhTsgPU5exOrNUtuQgkR7Vo8PeyL0ZdsyWo74Zeyc0iW4M1" +
        "MKLcN8GZp/2F5fsuhyt5pHF9DP1CZfuDrVmSflFwvk+H1ghMDqZecISfdwo7f/iRzaxek5IENCrgOKpXEn9C3t8TwBtsiLbvYMnMBpRysiP1" +
        "zHNakfDNDFJf7Dh9m/dTbMVA7L4AOXGUMhj6/oEayQ1h2ogVkiNZbp3acoIT6aCsFTn+XcbazEhQsyPLmDcMvz2trJzih6XKUscfFnA9tRIS" +
        "lc4E1Zy9PLFnciDDkFVYbCEE1DtFOxDoRAvXFBYoRfdVGoS/93ZoeP26jYJ0drJifWEcvgOXbzduOwIDAQAB";
    PublicKey KEY;


    @Test
    public void serializeToJSON() throws Exception {
        assertEquals(fixture("fixtures/add-user-operation.json"), MAPPER.writeValueAsString(OperationWrapper.of(ADD_USER)));
    }

    @Test
    public void deserializeFromJSON() throws Exception {
        assertEquals(OperationWrapper.of(ADD_USER), MAPPER.readValue(fixture("fixtures/add-user-operation.json"), OperationWrapper.class));
    }

    @Test
    public void serializesMutateDoorStateToJSON() throws Exception {
        final MutateDoorState state = ImmutableMutateDoorState.builder()
            .locked(false)
            .build();
        assertEquals(fixture("fixtures/change-door-state.json"), MAPPER.writeValueAsString(state));
    }

    @Test
    public void deserializeMutateDoorStateFromJSON() throws Exception {
        final MutateDoorState state = ImmutableMutateDoorState.builder()
            .locked(false)
            .build();
        assertEquals(state, MAPPER.readValue(fixture("fixtures/change-door-state.json"), MutateDoorState.class));
    }

    @Test
    public void deserializeMutateDoorStateWithNoDurationFromJSON() throws Exception {
        final MutateDoorState state = ImmutableMutateDoorState.builder()
            .locked(false)
            .build();
        assertEquals(state, MAPPER.readValue("{\"type\":\"MUTATE_LOCK\",\"locked\":false}", MutateDoorState.class));
    }

    @Test
    public void deserializeMutateDoorStateWithMinDurationFromJSON() throws Exception {
        final MutateDoorState state = ImmutableMutateDoorState.builder()
            .locked(false)
            .build();
        assertEquals(state, MAPPER.readValue("{\"type\":\"MUTATE_LOCK\",\"locked\":false,\"duration\":1}", MutateDoorState.class));
    }

    @Test
    public void deserializeMutateDoorStateWithMaxDurationFromJSON() throws Exception {
        final MutateDoorState state = ImmutableMutateDoorState.builder()
            .locked(false)
            .build();
        assertEquals(state, MAPPER.readValue("{\"type\":\"MUTATE_LOCK\",\"locked\":false,\"duration\":60}", MutateDoorState.class));
    }

    @Test
    public void deserializeMutateDoorStateWithHugeDurationFromJSON() throws Exception {
        final MutateDoorState state = ImmutableMutateDoorState.builder()
            .locked(false)
            .build();
        assertEquals(state, MAPPER.readValue("{\"type\":\"MUTATE_LOCK\",\"locked\":false,\"duration\":61}", MutateDoorState.class));
    }

    @Test
    public void deserializeMutateDoorStateWithTinyDurationFromJSON() throws Exception {
        final MutateDoorState state = ImmutableMutateDoorState.builder()
            .locked(false)
            .build();
        assertEquals(state, MAPPER.readValue("{\"type\":\"MUTATE_LOCK\",\"locked\":false,\"duration\":0.1}", MutateDoorState.class));
    }

}
