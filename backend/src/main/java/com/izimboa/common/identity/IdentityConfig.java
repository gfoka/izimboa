package com.izimboa.common.identity;

import com.izimboa.common.IdentityAdmin;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Compte de service Keycloak si KEYCLOAK_API_CLIENT_SECRET est fourni (serveur) ; sinon une version en mémoire
 * qui garde les rôles le temps du processus (développement, tests) et journalise chaque changement.
 */
@Configuration
class IdentityConfig {

    private static final Logger log = LoggerFactory.getLogger(IdentityConfig.class);

    @Bean
    IdentityAdmin identityAdmin(@Value("${izimboa.identity.admin-base:http://keycloak:8080}") String base,
                                @Value("${izimboa.identity.realm:izimboa}") String realm,
                                @Value("${izimboa.identity.client-id:izimboa-api}") String clientId,
                                @Value("${izimboa.identity.client-secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            log.warn("Gestion des comptes Keycloak désactivée : KEYCLOAK_API_CLIENT_SECRET absent (rôles gardés en mémoire).");
            return new InMemory();
        }
        return new KeycloakIdentityAdmin(base, realm, clientId, secret);
    }

    /** Pour le développement et les tests : aucun appel réseau. */
    static class InMemory implements IdentityAdmin {
        private final Map<UUID, Set<String>> roles = new ConcurrentHashMap<>();

        @Override public boolean enabled() { return false; }
        @Override public Optional<Account> find(UUID id) { return Optional.empty(); }
        @Override public Optional<Account> findByLogin(String login) { return Optional.empty(); }

        @Override
        public List<Account> withRole(String role) {
            return roles.entrySet().stream().filter(e -> e.getValue().contains(role))
                    .map(e -> new Account(e.getKey(), null, null, null, null, null, true)).toList();
        }

        @Override
        public void updateName(UUID id, String firstName, String lastName, String email) {
            log.info("Compte {} (non envoyé à Keycloak) : {} {}", id, firstName, lastName);
        }

        @Override
        public void addRole(UUID id, String role) {
            roles.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(role);
            log.info("Rôle « {} » donné au compte {} (en mémoire)", role, id);
        }

        @Override
        public void removeRole(UUID id, String role) {
            var set = roles.get(id);
            if (set != null) set.remove(role);
            log.info("Rôle « {} » retiré au compte {} (en mémoire)", role, id);
        }

        public boolean has(UUID id, String role) {
            return roles.getOrDefault(id, Set.of()).contains(role);
        }
    }
}
