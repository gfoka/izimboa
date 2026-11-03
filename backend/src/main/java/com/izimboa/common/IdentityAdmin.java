package com.izimboa.common;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gestion des comptes Keycloak par l'API Izimboa (rôles, nom affiché), pour que personne n'ait à ouvrir
 * la console Keycloak au quotidien (décision 74). Compte de service « izimboa-api » du realm izimboa.
 */
public interface IdentityAdmin {

    record Account(UUID id, String username, String firstName, String lastName, String email, String phone,
                   boolean enabled) {}

    /** Problème lisible (e-mail déjà pris, gestion des comptes indisponible…). */
    class IdentityException extends RuntimeException {
        private final int status;
        public IdentityException(int status, String message) { super(message); this.status = status; }
        public int status() { return status; }
    }

    /** false : pas de compte de service configuré (développement, tests) ; les changements sont seulement journalisés. */
    boolean enabled();

    Optional<Account> find(UUID id);

    /** Recherche par numéro (+237…), e-mail ou identifiant de connexion. */
    Optional<Account> findByLogin(String phoneEmailOrUsername);

    List<Account> withRole(String realmRole);

    void updateName(UUID id, String firstName, String lastName, String email);

    void addRole(UUID id, String realmRole);

    void removeRole(UUID id, String realmRole);
}
