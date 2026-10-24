package com.izimboa.delivery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.izimboa.PostgresIntegrationTest;
import com.izimboa.delivery.dispatch.DispatchService;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Attribution automatique au livreur libre le plus proche, refus → livreur suivant, acceptation,
 * puis position en direct pour le vendeur et le destinataire. Scène à Bonabéri, loin des autres tests.
 */
class DispatchAndLiveTrackingIntegrationTest extends PostgresIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired DispatchService dispatch;
    @Autowired JdbcTemplate jdbc;

    final RequestPostProcessor vendeur = user(UUID.randomUUID(), "vendeur");
    final UUID procheId = UUID.randomUUID();
    final UUID suivantId = UUID.randomUUID();
    final RequestPostProcessor proche = user(procheId, "livreur");     // ≈ 150 m du ramassage
    final RequestPostProcessor suivant = user(suivantId, "livreur");   // ≈ 900 m
    final RequestPostProcessor lointain = user(UUID.randomUUID(), "livreur"); // Akwa, hors rayon

    ResultActions postJson(String url, RequestPostProcessor who, String body) throws Exception {
        return mvc.perform(post(url).with(who).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    static String point(double lat, double lng) {
        return "{\"lat\":%s,\"lng\":%s,\"accuracy\":8,\"recordedAt\":\"%s\"}".formatted(lat, lng, Instant.now());
    }

    void online(RequestPostProcessor who, double lat, double lng) throws Exception {
        postJson("/v1/delivery/courier/online", who, "{\"position\":" + point(lat, lng) + "}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.online").value(true));
    }

    @Test
    void laCourseVaAuPlusProcheEtPasseAuSuivantEnCasDeRefus() throws Exception {
        online(proche, 4.0720, 9.6620);
        online(suivant, 4.0790, 9.6600);
        online(lointain, 4.0511, 9.7679);

        var body = postJson("/v1/delivery/deliveries", vendeur, """
                {"pickup":{"lat":4.0710,"lng":9.6610},"pickupAddress":"Bonabéri, marché","pickupPhone":"699000011",
                 "dropoff":{"lat":4.0800,"lng":9.6700},"dropoffAddress":"Bonabéri, Mabanda","recipientName":"Paul",
                 "recipientPhone":"677000012","codAmount":5000}""")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");
        String trackingUrl = JsonPath.read(body, "$.trackingUrl");
        String token = trackingUrl.substring(trackingUrl.lastIndexOf('/') + 1);

        // 1. Proposée au plus proche, qui voit ce qu'il gagnera (65 % de 1 000).
        dispatch.runOnce();
        var offer = mvc.perform(get("/v1/delivery/courier/offers/current").with(proche))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.delivery.id").value(id))
                .andExpect(jsonPath("$.courierEarning").value(650))
                .andExpect(jsonPath("$.delivery.codAmount").value(5000))
                .andExpect(jsonPath("$.secondsLeft").isNumber())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/v1/delivery/courier/offers/current").with(suivant)).andExpect(status().isNoContent());
        mvc.perform(get("/v1/delivery/courier/offers/current").with(lointain)).andExpect(status().isNoContent());

        // 2. Refus : au tour suivant, la course passe au livreur suivant.
        mvc.perform(post("/v1/delivery/courier/offers/" + JsonPath.read(offer, "$.offerId") + "/decline").with(proche))
                .andExpect(status().isNoContent());
        dispatch.runOnce();
        mvc.perform(get("/v1/delivery/courier/offers/current").with(proche)).andExpect(status().isNoContent());
        var offer2 = mvc.perform(get("/v1/delivery/courier/offers/current").with(suivant))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Un autre livreur ne peut pas accepter à sa place.
        String offer2Id = JsonPath.read(offer2, "$.offerId");
        mvc.perform(post("/v1/delivery/courier/offers/" + offer2Id + "/accept").with(proche)).andExpect(status().isNotFound());

        // 3. Acceptation : la livraison lui est attribuée ; plus de nouvelle proposition.
        mvc.perform(post("/v1/delivery/courier/offers/" + offer2Id + "/accept").with(suivant))
                .andExpect(status().isOk()).andExpect(jsonPath("$.deliveryId").value(id));
        mvc.perform(get("/v1/delivery/deliveries/" + id).with(vendeur))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.courierId").value(suivantId.toString()));
        dispatch.runOnce();
        mvc.perform(get("/v1/delivery/courier/offers/current").with(suivant)).andExpect(status().isNoContent());

        // 4. Position en direct : le vendeur la voit dès l'attribution, le destinataire seulement en route.
        postJson("/v1/delivery/courier/positions", suivant,
                "{\"points\":[" + point(4.0760, 9.6605) + "," + point(4.0750, 9.6608) + "]}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.saved").value(2));
        mvc.perform(get("/v1/delivery/deliveries/" + id + "/courier-position").with(vendeur))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lat").isNumber());
        mvc.perform(get("/v1/delivery/track/" + token + "/courier-position")).andExpect(status().isNoContent());
        mvc.perform(get("/v1/delivery/deliveries/" + id + "/courier-position").with(proche)).andExpect(status().isNotFound());

        postJson("/v1/delivery/deliveries/" + id + "/pickup", suivant, "{}").andExpect(status().isOk());
        postJson("/v1/delivery/deliveries/" + id + "/depart", suivant, "{}").andExpect(status().isOk());
        mvc.perform(get("/v1/delivery/track/" + token + "/courier-position"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lng").isNumber());

        // 5. Hors ligne : plus de position envoyée ni montrée.
        mvc.perform(post("/v1/delivery/courier/offline").with(suivant))
                .andExpect(status().isOk()).andExpect(jsonPath("$.online").value(false));
        postJson("/v1/delivery/courier/positions", suivant, "{\"points\":[" + point(4.07, 9.66) + "]}")
                .andExpect(status().isUnprocessableContent());
        mvc.perform(get("/v1/delivery/track/" + token + "/courier-position")).andExpect(status().isNoContent());

        for (var c : new RequestPostProcessor[] {proche, lointain}) {
            mvc.perform(post("/v1/delivery/courier/offline").with(c)).andExpect(status().isOk());
        }
    }

    @Test
    void sansReponseLaCoursePasseAuSuivant() throws Exception {
        var a = user(UUID.randomUUID(), "livreur");
        var b = user(UUID.randomUUID(), "livreur");
        online(a, 4.0905, 9.6505);   // Bonabéri nord, ≈ 100 m
        online(b, 4.0950, 9.6550);   // ≈ 700 m
        var body = postJson("/v1/delivery/deliveries", vendeur, """
                {"pickup":{"lat":4.0900,"lng":9.6500},"pickupAddress":"Bonabéri nord","pickupPhone":"699000021",
                 "dropoff":{"lat":4.0850,"lng":9.6600},"dropoffAddress":"Bonabéri","recipientName":"Marie",
                 "recipientPhone":"677000022"}""")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");

        dispatch.runOnce();
        mvc.perform(get("/v1/delivery/courier/offers/current").with(a))
                .andExpect(status().isOk()).andExpect(jsonPath("$.delivery.id").value(id));

        // Le délai de réponse est dépassé (on avance l'échéance plutôt que d'attendre 30 s).
        jdbc.update("update delivery.delivery_offer set expires_at = now() - interval '1 second' where delivery_id = ?", UUID.fromString(id));
        dispatch.runOnce();
        mvc.perform(get("/v1/delivery/courier/offers/current").with(a)).andExpect(status().isNoContent());
        mvc.perform(get("/v1/delivery/courier/offers/current").with(b))
                .andExpect(status().isOk()).andExpect(jsonPath("$.delivery.id").value(id));

        // Le vendeur annule : la proposition en cours est retirée au tour suivant.
        postJson("/v1/delivery/deliveries/" + id + "/cancel", vendeur, "{}").andExpect(status().isOk());
        dispatch.runOnce();
        mvc.perform(get("/v1/delivery/courier/offers/current").with(b)).andExpect(status().isNoContent());
        for (var c : new RequestPostProcessor[] {a, b}) mvc.perform(post("/v1/delivery/courier/offline").with(c));
    }

    @Test
    void seulsLesLivreursUtilisentLesRoutesLivreur() throws Exception {
        mvc.perform(post("/v1/delivery/courier/online").with(vendeur)).andExpect(status().isForbidden());
        mvc.perform(get("/v1/delivery/courier/offers/current").with(vendeur)).andExpect(status().isForbidden());
        mvc.perform(get("/v1/delivery/courier/status").with(lointain))
                .andExpect(status().isOk()).andExpect(jsonPath("$.online").value(false));
    }
}
