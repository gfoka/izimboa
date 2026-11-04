package com.izimboa.delivery.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.izimboa.PostgresIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Webhook Meta : signature obligatoire, messages des clients rattachés à leur livraison (décision 75). */
@TestPropertySource(properties = {"izimboa.whatsapp.app-secret=secret-de-test", "izimboa.whatsapp.verify-token=jeton-de-test"})
class WhatsAppWebhookIntegrationTest extends PostgresIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired InboundMessageRepository inbound;

    static String sign(String body) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secret-de-test".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    static String message(String waId, String from, String text) {
        return """
            {"object":"whatsapp_business_account","entry":[{"id":"112090771912051","changes":[{"field":"messages","value":{
              "messaging_product":"whatsapp","metadata":{"phone_number_id":"112115108576086"},
              "contacts":[{"profile":{"name":"Awa"},"wa_id":"%s"}],
              "messages":[{"from":"%s","id":"%s","timestamp":"1793760000","type":"text","text":{"body":"%s"}}]}}]}]}"""
                .formatted(from, from, waId, text);
    }

    @Test
    void verificationDeLAdresseParMeta() throws Exception {
        mvc.perform(get("/v1/whatsapp/webhook?hub.mode=subscribe&hub.verify_token=jeton-de-test&hub.challenge=12345"))
                .andExpect(status().isOk()).andExpect(content().string("12345"));
        mvc.perform(get("/v1/whatsapp/webhook?hub.mode=subscribe&hub.verify_token=faux&hub.challenge=12345"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unMessageDuDestinataireEstRattacheASaLivraison() throws Exception {
        // Une livraison pour un numéro unique, puis ce destinataire écrit au numéro Izimboa.
        String digits = "2376" + String.format("%08d", Math.abs(UUID.randomUUID().getMostSignificantBits() % 100_000_000L));
        String created = mvc.perform(post("/v1/delivery/deliveries").with(user(UUID.randomUUID(), "client", "vendeur"))
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"pickup":{"lat":4.0511,"lng":9.7679},"pickupAddress":"Akwa","pickupPhone":"6 99 00 00 01",
                     "dropoff":{"lat":4.0600,"lng":9.7700},"dropoffAddress":"Akwa nord","recipientName":"Awa",
                     "recipientPhone":"+%s","express":false,"codAmount":0}""".formatted(digits)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String deliveryId = JsonPath.read(created, "$.id");

        String waId = "wamid.test-" + UUID.randomUUID();
        String body = message(waId, digits, "Mon colis est abîmé");
        mvc.perform(post("/v1/whatsapp/webhook").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("X-Hub-Signature-256", "sha256=00")).andExpect(status().isForbidden());
        mvc.perform(post("/v1/whatsapp/webhook").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("X-Hub-Signature-256", sign(body))).andExpect(status().isOk());
        // Meta renvoie parfois le même message : enregistré une seule fois.
        mvc.perform(post("/v1/whatsapp/webhook").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("X-Hub-Signature-256", sign(body))).andExpect(status().isOk());

        var saved = inbound.findAll().stream().filter(m -> m.getWaMessageId().equals(waId)).toList();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getDeliveryId()).hasToString(deliveryId);
        assertThat(saved.getFirst().getBody()).isEqualTo("Mon colis est abîmé");
        assertThat(saved.getFirst().getFromPhone()).isEqualTo("+" + digits);

        // Ne pas laisser de course à attribuer aux autres tests.
        mvc.perform(post("/v1/delivery/deliveries/" + deliveryId + "/cancel").with(user(UUID.randomUUID(), "operateur"))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
    }
}
