package com.izimboa.common.contact;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.izimboa.PostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Formulaire « Entreprises » du site : enregistré sans connexion, protégé contre les robots et l'abus. */
class ContactRequestIntegrationTest extends PostgresIntegrationTest {

    @Autowired MockMvc mvc;

    static String body(String company, String phone, String website) {
        return """
            {"product":"delivery","kind":"business","company":"%s","contactName":"Awa Ngo","phone":"%s",
             "email":"awa@example.com","city":"Douala","activity":"Vente de chaussures en ligne","volume":"20-100",
             "message":"Nous livrons 50 colis par mois.","lang":"fr","website":"%s"}""".formatted(company, phone, website);
    }

    ResultActions send(String ip, String body) throws Exception {
        return mvc.perform(post("/v1/contact-requests").contentType(MediaType.APPLICATION_JSON).content(body)
                .with(r -> { r.setRemoteAddr(ip); return r; }));
    }

    @Test
    void uneEntrepriseEnvoieSaDemandeSansCompte() throws Exception {
        String company = "Boutique " + UUID.randomUUID();
        send("10.1.0.1", body(company, "6 99 12 34 56", "")).andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists());

        mvc.perform(get("/v1/contact-requests").with(user(UUID.randomUUID(), "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].company", hasItem(company)))
                .andExpect(jsonPath("$[*].phone", hasItem("+237699123456")));
    }

    @Test
    void laListeEstReserveeALEquipe() throws Exception {
        mvc.perform(get("/v1/contact-requests").with(user(UUID.randomUUID(), "client", "vendeur"))).andExpect(status().isForbidden());
        mvc.perform(get("/v1/contact-requests")).andExpect(status().isUnauthorized());
    }

    @Test
    void numeroInvalideOuChampManquantRefuses() throws Exception {
        send("10.1.0.2", body("Boutique", "12", "")).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("invalid-request"));
        send("10.1.0.2", body("", "699123456", "")).andExpect(status().isBadRequest());
    }

    @Test
    void lesRobotsSontIgnoresSansLeSavoir() throws Exception {
        String company = "Robot " + UUID.randomUUID();
        send("10.1.0.3", body(company, "699123456", "http://spam.example")).andExpect(status().isAccepted());
        mvc.perform(get("/v1/contact-requests").with(user(UUID.randomUUID(), "admin")))
                .andExpect(jsonPath("$[*].company", not(hasItem(company))));
    }

    @Test
    void cinqDemandesParHeureEtParAdresseAuPlus() throws Exception {
        for (int i = 0; i < ContactService.MAX_PER_HOUR; i++) {
            send("10.1.0.4", body("Boutique " + i, "699123456", "")).andExpect(status().isCreated());
        }
        send("10.1.0.4", body("Boutique de trop", "699123456", "")).andExpect(status().isTooManyRequests());
        send("10.1.0.5", body("Autre boutique", "699123456", "")).andExpect(status().isCreated());
    }
}
