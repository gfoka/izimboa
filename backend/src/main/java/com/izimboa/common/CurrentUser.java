package com.izimboa.common;

import java.util.Set;
import java.util.UUID;

/**
 * Utilisateur connecté, tel que décrit par son jeton Keycloak.
 * Les autres modules ne connaissent que cet identifiant : jamais d'informations de connexion.
 */
public record CurrentUser(UUID id, String email, String name, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
