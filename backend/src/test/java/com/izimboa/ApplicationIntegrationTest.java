package com.izimboa;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.izimboa.common.security.KeycloakJwtConverter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Démarre toute l'application sur un vrai PostgreSQL (Docker) : migrations Flyway, JPA, sécurité.
 * Ignoré automatiquement si Docker n'est pas disponible ; toujours exécuté dans GitHub Actions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired MockMvc mvc;

    @Test
    void lApplicationEstEnBonneSante() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void meEnregistreLeProfilEnBase() throws Exception {
        var token = jwt().jwt(j -> j.subject("7f9a2c3e-2222-4b6c-8d3e-abcdefabcdef").claim("email", "x@izimboa.local")
                        .claim("realm_access", Map.of("roles", List.of("client"))))
                .authorities(KeycloakJwtConverter::authorities);
        mvc.perform(get("/v1/me").with(token)).andExpect(status().isOk());
        mvc.perform(get("/v1/me").with(token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.memberSince").exists());
    }
}
