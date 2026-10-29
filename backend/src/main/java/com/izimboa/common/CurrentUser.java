package com.izimboa.common;

import java.util.Set;
import java.util.UUID;

/**
 * Utilisateur connecté, tel que décrit par son jeton Keycloak.
 * Les autres modules ne connaissent que cet identifiant : jamais d'informations de connexion.
 * {@code phone} : numéro vérifié par code WhatsApp (format +237…), {@code null} pour un compte e-mail.
 */
public record CurrentUser(UUID id, String email, String name, String phone, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /** Opérateur ou administrateur. */
    public boolean isStaff() {
        return Roles.isStaff(roles);
    }
}
