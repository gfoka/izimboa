package com.izimboa.delivery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.izimboa.PostgresIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Inscription d'un livreur (dossier, documents photographiés) puis vérification par l'équipe (décision 74). */
class CourierOnboardingIntegrationTest extends PostgresIntegrationTest {

    /** Début d'un fichier JPEG : suffisant pour la vérification du format. */
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F', 0, 1, 1, 0};

    @Autowired MockMvc mvc;

    final UUID candidatId = UUID.randomUUID();
    final RequestPostProcessor candidat = user(candidatId, "client");
    final RequestPostProcessor validateur = user(UUID.randomUUID(), "validateur");
    final RequestPostProcessor admin = user(UUID.randomUUID(), "admin");

    List<Integer> pages(String json, String code) {
        return JsonPath.read(json, "$.documents[?(@.type.code == '" + code + "')].type.pages[*].id");
    }

    @Test
    void unNouveauLivreurSInscritPuisEstValide() throws Exception {
        // 1. Premier passage : dossier vide, les deux documents du modèle de départ sont demandés.
        String json = mvc.perform(get("/v1/delivery/onboarding").with(candidat)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.canWork").value(false))
                .andExpect(jsonPath("$.documents[?(@.type.code == 'CNI')].state").value(Matchers.contains("MISSING")))
                .andExpect(jsonPath("$.documents[?(@.type.code == 'SELFIE')].type.pages[0].camera").value(Matchers.contains("FRONT")))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(post("/v1/delivery/onboarding/submit").with(candidat)).andExpect(status().isUnprocessableContent());

        // 2. Prénom et nom obligatoires, e-mail facultatif.
        mvc.perform(put("/v1/delivery/onboarding/profile").with(candidat).contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"lastName\":\"Ngo\"}")).andExpect(status().isUnprocessableContent());
        mvc.perform(put("/v1/delivery/onboarding/profile").with(candidat).contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\" Awa \",\"lastName\":\"Ngo\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Awa"));

        // 3. La CNI : numéro, date de validité et les deux pages. Sans verso : refusé.
        var cni = pages(json, "CNI");
        String future = LocalDate.now().plusYears(3).toString();
        mvc.perform(multipart("/v1/delivery/onboarding/documents/CNI").file(new MockMultipartFile("page-" + cni.get(0), "r.jpg", "image/jpeg", JPEG))
                        .param("number", "123456789").param("expiresOn", future).with(candidat))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(multipart("/v1/delivery/onboarding/documents/CNI")
                        .file(new MockMultipartFile("page-" + cni.get(0), "r.jpg", "image/jpeg", JPEG))
                        .file(new MockMultipartFile("page-" + cni.get(1), "v.jpg", "image/jpeg", "pas une image".getBytes()))
                        .param("number", "123456789").param("expiresOn", future).with(candidat))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(multipart("/v1/delivery/onboarding/documents/CNI")
                        .file(new MockMultipartFile("page-" + cni.get(0), "r.jpg", "image/jpeg", JPEG))
                        .file(new MockMultipartFile("page-" + cni.get(1), "v.jpg", "image/jpeg", JPEG))
                        .param("number", "123456789").param("expiresOn", future).with(candidat))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents[?(@.type.code == 'CNI')].state").value(Matchers.contains("OK")));
        var selfie = pages(json, "SELFIE");
        mvc.perform(multipart("/v1/delivery/onboarding/documents/SELFIE")
                        .file(new MockMultipartFile("page-" + selfie.get(0), "s.jpg", "image/jpeg", JPEG)).with(candidat))
                .andExpect(status().isOk()).andExpect(jsonPath("$.complete").value(true));

        // 4. Envoi : en attente de vérification.
        mvc.perform(post("/v1/delivery/onboarding/submit").with(candidat))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));

        // 5. Le validateur voit le dossier et les photos ; le candidat, lui, n'a pas accès à la liste.
        mvc.perform(get("/v1/delivery/couriers?status=PENDING").with(candidat)).andExpect(status().isForbidden());
        mvc.perform(get("/v1/delivery/couriers?status=PENDING").with(validateur)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userId", Matchers.hasItem(candidatId.toString())));
        String detail = mvc.perform(get("/v1/delivery/couriers/" + candidatId).with(validateur)).andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Awa"))
                .andReturn().getResponse().getContentAsString();
        List<String> docIds = JsonPath.read(detail, "$.documents[?(@.type.code == 'CNI')].submitted.id");
        mvc.perform(get("/v1/delivery/couriers/" + candidatId + "/documents/" + docIds.getFirst() + "/pages/" + cni.get(1)).with(validateur))
                .andExpect(status().isOk()).andExpect(content().bytes(JPEG))
                .andExpect(header().string("Cache-Control", Matchers.containsString("no-store")));

        // 6. Refus avec motif, correction, puis validation.
        mvc.perform(post("/v1/delivery/couriers/" + candidatId + "/reject").with(validateur).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"\"}")).andExpect(status().isUnprocessableContent());
        mvc.perform(post("/v1/delivery/couriers/" + candidatId + "/reject").with(validateur).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Selfie flou\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        mvc.perform(get("/v1/delivery/onboarding").with(candidat))
                .andExpect(jsonPath("$.status").value("REJECTED")).andExpect(jsonPath("$.reason").value("Selfie flou"));
        mvc.perform(post("/v1/delivery/onboarding/submit").with(candidat)).andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(post("/v1/delivery/couriers/" + candidatId + "/activate").with(validateur))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACTIVE"));

        // 7. Devenu livreur (rôle dans le jeton), il peut passer en ligne.
        var livreur = user(candidatId, "livreur");
        mvc.perform(get("/v1/delivery/onboarding").with(livreur)).andExpect(jsonPath("$.canWork").value(true));
        mvc.perform(post("/v1/delivery/courier/online").with(livreur)).andExpect(status().isOk());
        // Suspendu : hors ligne tout de suite, et il ne peut plus repasser en ligne.
        mvc.perform(post("/v1/delivery/couriers/" + candidatId + "/suspend").with(admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Colis non remis\"}")).andExpect(jsonPath("$.status").value("SUSPENDED"));
        mvc.perform(get("/v1/delivery/courier/status").with(livreur)).andExpect(jsonPath("$.online").value(false));
        mvc.perform(post("/v1/delivery/courier/online").with(livreur))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("courier-file-incomplete"));
    }

    @Test
    void unLivreurDejaActifADesJoursPourCompleterSonDossier() throws Exception {
        var ancien = user(UUID.randomUUID(), "livreur");
        mvc.perform(get("/v1/delivery/onboarding").with(ancien)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.complete").value(false))
                .andExpect(jsonPath("$.canWork").value(true))
                .andExpect(jsonPath("$.deadline").value(LocalDate.now(java.time.ZoneId.of("Africa/Douala")).plusDays(7).toString()));
        mvc.perform(post("/v1/delivery/courier/online").with(ancien)).andExpect(status().isOk());
        mvc.perform(post("/v1/delivery/courier/offline").with(ancien)).andExpect(status().isOk());
    }

    @Test
    void lAdministrationRegleLeModeleDeDocuments() throws Exception {
        String code = "PERMIS_" + Long.toString(System.nanoTime(), 36).toUpperCase();
        String body = """
            {"code":"%s","name":"Permis de conduire","mandatory":false,"numberRequirement":"OPTIONAL",
             "expiryRequired":true,"position":5,"pages":[{"name":"Recto"},{"name":"Verso"}]}""".formatted(code);
        mvc.perform(post("/v1/delivery/document-types").with(validateur).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        String created = mvc.perform(post("/v1/delivery/document-types").with(admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.pages.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");
        int recto = JsonPath.read(created, "$.pages[0].id");
        mvc.perform(post("/v1/delivery/document-types").with(admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableContent());
        // Une seule page désormais, la page « Recto » garde son identifiant ; puis désactivation.
        mvc.perform(put("/v1/delivery/document-types/" + id).with(admin).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Permis","mandatory":false,"numberRequirement":"NONE","expiryRequired":false,"active":false,
                 "pages":[{"id":%d,"name":"Recto"}]}""".formatted(recto)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pages[0].id").value(recto))
                .andExpect(jsonPath("$.pages.length()").value(1)).andExpect(jsonPath("$.active").value(false));
        // Un document désactivé n'est plus demandé aux livreurs.
        mvc.perform(get("/v1/delivery/onboarding").with(user(UUID.randomUUID(), "client")))
                .andExpect(jsonPath("$.documents[?(@.type.code == '" + code + "')]").isEmpty());
    }

    @Test
    void lEquipeSeGereDansIzimboa() throws Exception {
        mvc.perform(get("/v1/admin/team").with(validateur)).andExpect(status().isForbidden());
        mvc.perform(get("/v1/admin/team").with(admin)).andExpect(status().isOk());
        mvc.perform(post("/v1/admin/team").with(admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"+237699000099\",\"role\":\"super\"}")).andExpect(status().isUnprocessableContent());
        // Sans Keycloak (tests), aucun compte n'est retrouvé.
        mvc.perform(post("/v1/admin/team").with(admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"+237699000099\",\"role\":\"validateur\"}")).andExpect(status().isNotFound());
    }
}
