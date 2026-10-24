package com.izimboa.delivery;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.izimboa.PostgresIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Parcours complets d'une livraison, de bout en bout, sur une vraie base. */
class DeliveryFlowIntegrationTest extends PostgresIntegrationTest {

    // Repères à Douala : Akwa → Bonamoussadi ≈ 4,6 km à vol d'oiseau, ≈ 6,1 km par la route → palier 1 500.
    static final String AKWA = "{\"lat\":4.0511,\"lng\":9.7679}";
    static final String BONAMOUSSADI = "{\"lat\":4.0880,\"lng\":9.7500}";
    static final String AKWA_VOISIN = "{\"lat\":4.0600,\"lng\":9.7700}";   // ≈ 1,4 km par la route → 1 000
    static final String YAOUNDE = "{\"lat\":3.8480,\"lng\":11.5021}";

    @Autowired MockMvc mvc;

    final RequestPostProcessor vendeur = user(UUID.randomUUID(), "client", "vendeur");
    final UUID livreurId = UUID.randomUUID();
    final RequestPostProcessor livreur = user(livreurId, "livreur");
    final RequestPostProcessor autreLivreur = user(UUID.randomUUID(), "livreur");
    final RequestPostProcessor operateur = user(UUID.randomUUID(), "operateur");
    final RequestPostProcessor admin = user(UUID.randomUUID(), "admin");

    static String livraison(String from, String to, boolean express, long cod) {
        return """
            {"pickup":%s,"pickupAddress":"Rue Joss, Akwa","pickupPhone":"6 99 00 00 01",
             "dropoff":%s,"dropoffAddress":"Bonamoussadi, carrefour Kotto","dropoffLandmark":"Derrière la boulangerie",
             "recipientName":"Awa Ngo","recipientPhone":"699-00-00-02","packageDescription":"Chaussures",
             "express":%s,"codAmount":%d}""".formatted(from, to, express, cod);
    }

    ResultActions postJson(String url, RequestPostProcessor who, String body) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(who == null ? req : req.with(who));
    }

    String create(String body) throws Exception {
        var res = postJson("/v1/delivery/deliveries", vendeur, body).andExpect(status().isCreated()).andReturn();
        return JsonPath.read(res.getResponse().getContentAsString(), "$.id");
    }

    String url(String id, String action) {
        return "/v1/delivery/deliveries/" + id + (action == null ? "" : "/" + action);
    }

    void assignAndStart(String id) throws Exception {
        postJson(url(id, "assign"), operateur, "{\"courierId\":\"" + livreurId + "\"}").andExpect(status().isOk());
        postJson(url(id, "pickup"), livreur, "{\"lat\":4.0511,\"lng\":9.7679}").andExpect(status().isOk());
        postJson(url(id, "depart"), livreur, "{}").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    void simulationDePrixPubliqueSansConnexion() throws Exception {
        postJson("/v1/delivery/quotes", null, "{\"pickup\":" + AKWA + ",\"dropoff\":" + BONAMOUSSADI
                + ",\"express\":true,\"codAmount\":15000}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("XAF"))
                .andExpect(jsonPath("$.basePrice").value(1500))
                .andExpect(jsonPath("$.expressSurcharge").value(500))
                .andExpect(jsonPath("$.codCommission").value(225))
                .andExpect(jsonPath("$.total").value(2225));

        postJson("/v1/delivery/quotes", null, "{\"pickup\":" + AKWA + ",\"dropoff\":" + AKWA_VOISIN + "}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1000));

        postJson("/v1/delivery/quotes", null, "{\"pickup\":" + AKWA + ",\"dropoff\":" + YAOUNDE + "}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("outside-service-area"));

        mvc.perform(get("/v1/delivery/tariffs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.tiers[0].maxDistanceKm").value(3.0))
                .andExpect(jsonPath("$.tiers[0].price").value(1000))
                .andExpect(jsonPath("$.codCommissionPercent").value(1.5));
    }

    @Test
    void livraisonAvecEncaissementDeBoutEnBout() throws Exception {
        // 1. Création : prix de la grille, frais retenus sur l'encaissement, code de remise pour le vendeur.
        var res = mvc.perform(post("/v1/delivery/deliveries").with(vendeur).header("Idempotency-Key", "cmd-42-" + livreurId)
                        .contentType(MediaType.APPLICATION_JSON).content(livraison(AKWA, BONAMOUSSADI, true, 15000)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.reference").value(org.hamcrest.Matchers.matchesPattern("IZ-[2-9A-Z]{6}")))
                .andExpect(jsonPath("$.pickupPhone").value("+237699000001"))
                .andExpect(jsonPath("$.recipientPhone").value("+237699000002"))
                .andExpect(jsonPath("$.pricing.totalFee").value(2225))
                .andExpect(jsonPath("$.pricing.feePayment").value("DEDUCTED_FROM_COD"))
                .andExpect(jsonPath("$.pricing.sellerPayout").value(12775))
                .andExpect(jsonPath("$.handoverCode").value(org.hamcrest.Matchers.matchesPattern("\\d{4}")))
                .andReturn();
        String body = res.getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");
        String code = JsonPath.read(body, "$.handoverCode");
        String trackingUrl = JsonPath.read(body, "$.trackingUrl");

        // Même clé d'idempotence (réseau coupé, nouvel envoi) : même livraison, pas de doublon.
        mvc.perform(post("/v1/delivery/deliveries").with(vendeur).header("Idempotency-Key", "cmd-42-" + livreurId)
                        .contentType(MediaType.APPLICATION_JSON).content(livraison(AKWA, BONAMOUSSADI, true, 15000)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));

        // 2. Personne d'autre ne la voit ; seul un opérateur l'attribue.
        mvc.perform(get(url(id, null)).with(livreur)).andExpect(status().isNotFound());
        postJson(url(id, "assign"), vendeur, "{\"courierId\":\"" + livreurId + "\"}").andExpect(status().isForbidden());

        // 3. Attribution, ramassage, départ. Le livreur ne voit jamais le code.
        postJson(url(id, "assign"), operateur, "{\"courierId\":\"" + livreurId + "\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ASSIGNED"));
        mvc.perform(get(url(id, null)).with(livreur)).andExpect(status().isOk())
                .andExpect(jsonPath("$.handoverCode").doesNotExist());
        mvc.perform(get(url(id, null)).with(autreLivreur)).andExpect(status().isNotFound());
        mvc.perform(get("/v1/delivery/deliveries").with(livreur)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));
        postJson(url(id, "deliver"), livreur, "{\"code\":\"" + code + "\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("invalid-transition"));
        postJson(url(id, "pickup"), livreur, "{\"lat\":4.0511,\"lng\":9.7679}").andExpect(status().isOk());
        postJson(url(id, "cancel"), vendeur, "{}").andExpect(status().isConflict());
        postJson(url(id, "depart"), livreur, "{}").andExpect(status().isOk());

        // 4. Remise : un code faux est compté (même si la requête échoue), puis le bon code valide.
        String wrong = code.equals("0000") ? "1111" : "0000";
        postJson(url(id, "deliver"), livreur, "{\"code\":\"" + wrong + "\"}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("wrong-handover-code"));
        mvc.perform(get(url(id, null)).with(vendeur)).andExpect(jsonPath("$.handoverFailures").value(1));
        postJson(url(id, "deliver"), livreur, "{\"code\":\"" + code + "\",\"lat\":4.088,\"lng\":9.75}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.timeline.length()").value(5));

        // 5. Suivi public du destinataire : l'étape, sans données personnelles.
        String token = trackingUrl.substring(trackingUrl.lastIndexOf('/') + 1);
        mvc.perform(get("/v1/delivery/track/" + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.amountToPay").value(15000))
                .andExpect(jsonPath("$.recipientPhone").doesNotExist())
                .andExpect(jsonPath("$.handoverCode").doesNotExist());
        mvc.perform(get("/v1/delivery/track/inconnu")).andExpect(status().isNotFound());
    }

    @Test
    void destinataireAbsentDeuxFoisPuisRetourAuVendeur() throws Exception {
        String id = create(livraison(AKWA, AKWA_VOISIN, false, 0));
        assignAndStart(id);
        postJson(url(id, "fail"), livreur, "{\"reason\":\"Destinataire absent\"}")
                .andExpect(jsonPath("$.status").value("FAILED"));
        postJson(url(id, "retry"), livreur, "{}").andExpect(jsonPath("$.status").value("IN_TRANSIT"));
        postJson(url(id, "fail"), livreur, "{\"reason\":\"Toujours absent\"}").andExpect(status().isOk());
        postJson(url(id, "retry"), livreur, "{}").andExpect(status().isUnprocessableContent());
        postJson(url(id, "return"), livreur, "{}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.pricing.feePayment").value("PREPAID"))
                .andExpect(jsonPath("$.pricing.returnFee").value(500));
    }

    @Test
    void annulationGratuiteAvantRamassage() throws Exception {
        String id = create(livraison(AKWA, AKWA_VOISIN, false, 0));
        postJson(url(id, "cancel"), vendeur, "{\"reason\":\"Commande annulée par le client\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(get("/v1/delivery/deliveries?status=CANCELLED").with(vendeur))
                .andExpect(jsonPath("$[*].id", hasItem(id)));
        mvc.perform(get("/v1/delivery/deliveries?status=DELIVERED").with(vendeur))
                .andExpect(jsonPath("$[*].id", not(hasItem(id))));
    }

    @Test
    void controlesALaCreation() throws Exception {
        mvc.perform(post("/v1/delivery/deliveries").contentType(MediaType.APPLICATION_JSON)
                .content(livraison(AKWA, BONAMOUSSADI, false, 0))).andExpect(status().isUnauthorized());
        postJson("/v1/delivery/deliveries", livreur, livraison(AKWA, BONAMOUSSADI, false, 0))
                .andExpect(status().isForbidden());
        postJson("/v1/delivery/deliveries", vendeur, livraison(AKWA, YAOUNDE, false, 0))
                .andExpect(status().isUnprocessableContent());
        postJson("/v1/delivery/deliveries", vendeur, livraison(AKWA, BONAMOUSSADI, false, 0).replace("699-00-00-02", "12"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("invalid-request"));
        postJson("/v1/delivery/deliveries", vendeur, "{}").andExpect(status().isBadRequest());
    }

    @Test
    void seulLAdministrateurModifieLaGrille() throws Exception {
        String grille = """
            {"tiers":[{"maxDistanceKm":3,"price":1000},{"maxDistanceKm":7,"price":1500},{"maxDistanceKm":12,"price":2000}],
             "expressSurcharge":%d,"codCommissionPercent":1.5,"returnFeePercent":50}""";
        mvc.perform(put("/v1/delivery/tariffs").with(vendeur).contentType(MediaType.APPLICATION_JSON)
                .content(grille.formatted(600))).andExpect(status().isForbidden());
        mvc.perform(put("/v1/delivery/tariffs").with(admin).contentType(MediaType.APPLICATION_JSON)
                .content(grille.formatted(600))).andExpect(status().isOk())
                .andExpect(jsonPath("$.expressSurcharge").value(600));
        mvc.perform(put("/v1/delivery/tariffs").with(admin).contentType(MediaType.APPLICATION_JSON)
                .content(grille.formatted(500).replace("\"price\":1500", "\"price\":900")))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(put("/v1/delivery/tariffs").with(admin).contentType(MediaType.APPLICATION_JSON)
                .content(grille.formatted(500))).andExpect(status().isOk());
        mvc.perform(get("/v1/delivery/tariffs")).andExpect(jsonPath("$.expressSurcharge").value(500));
    }

    @Test
    void releveMeteoParLeLivreur() throws Exception {
        String releve = "{\"kind\":\"RAIN\",\"position\":{\"lat\":4.05,\"lng\":9.7}}";
        postJson("/v1/delivery/conditions", livreur, releve).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
        postJson("/v1/delivery/conditions", vendeur, releve).andExpect(status().isForbidden());
    }
}
