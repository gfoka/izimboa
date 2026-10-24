package com.izimboa;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.izimboa.common.security.KeycloakJwtConverter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base des tests qui démarrent toute l'application sur un vrai PostgreSQL.
 * - Dans GitHub Actions : une base jetable dans Docker (Testcontainers).
 * - Sans Docker : la base indiquée par TEST_DB_URL (TEST_DB_USER / TEST_DB_PASSWORD, défaut izimboa).
 * - Ni l'un ni l'autre : tests ignorés.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("com.izimboa.PostgresIntegrationTest#databaseAvailable")
public abstract class PostgresIntegrationTest {

    private static final String EXTERNAL_URL = System.getenv("TEST_DB_URL");
    private static PostgreSQLContainer container;

    static boolean databaseAvailable() {
        return EXTERNAL_URL != null || DockerClientFactory.instance().isDockerAvailable();
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        // Les tests lancent eux-mêmes les tours d'attribution (résultats prévisibles).
        registry.add("izimboa.delivery.dispatch.enabled", () -> "false");
        if (EXTERNAL_URL != null) {
            registry.add("spring.datasource.url", () -> EXTERNAL_URL);
            registry.add("spring.datasource.username", () -> envOr("TEST_DB_USER", "izimboa"));
            registry.add("spring.datasource.password", () -> envOr("TEST_DB_PASSWORD", "izimboa"));
            return;
        }
        synchronized (PostgresIntegrationTest.class) {
            if (container == null) {
                container = new PostgreSQLContainer("postgres:17-alpine");
                container.start();
            }
        }
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    private static String envOr(String name, String fallback) {
        String v = System.getenv(name);
        return v == null ? fallback : v;
    }

    /** Un utilisateur Keycloak fictif avec ses rôles. */
    protected static RequestPostProcessor user(UUID id, String... roles) {
        return jwt().jwt(j -> j.subject(id.toString()).claim("email", id + "@izimboa.test")
                        .claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(KeycloakJwtConverter::authorities);
    }
}
