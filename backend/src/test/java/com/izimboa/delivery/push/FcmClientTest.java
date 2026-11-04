package com.izimboa.delivery.push;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FcmClientTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void sansCompteDeServiceRienNestEnvoye() {
        var fcm = FcmClient.from("", json, Clock.systemUTC());
        assertFalse(fcm.enabled());
        assertEquals(FcmClient.Outcome.DISABLED, fcm.send("jeton", Map.of("type", "offer"), 30));
    }

    @Test
    void compteDeServiceEnBase64OuEnJson() throws Exception {
        var gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(gen.generateKeyPair().getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";
        String sa = json.writeValueAsString(Map.of("type", "service_account", "project_id", "izimboa",
                "client_email", "firebase-adminsdk@izimboa.iam.gserviceaccount.com", "private_key", pem,
                "token_uri", "https://oauth2.googleapis.com/token"));
        assertTrue(FcmClient.from(sa, json, Clock.systemUTC()).enabled());
        assertTrue(FcmClient.from(Base64.getEncoder().encodeToString(sa.getBytes(StandardCharsets.UTF_8)), json, Clock.systemUTC()).enabled());
        assertFalse(FcmClient.from("pas du json", json, Clock.systemUTC()).enabled());
    }
}
