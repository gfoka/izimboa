package com.izimboa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/** L'application complète démarre : migrations Flyway, JPA, sécurité. */
class ApplicationIntegrationTest extends PostgresIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void lApplicationEstEnBonneSante() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void meEnregistreLeProfilEnBase() throws Exception {
        var token = user(UUID.randomUUID(), "client");
        mvc.perform(get("/v1/me").with(token)).andExpect(status().isOk());
        mvc.perform(get("/v1/me").with(token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.memberSince").exists());
    }
}
