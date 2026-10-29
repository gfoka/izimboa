package com.izimboa.common.security;

import com.izimboa.common.CurrentUser;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Construit le {@link CurrentUser} à partir de l'authentification Spring. */
public final class CurrentUserResolver {

    private CurrentUserResolver() {}

    public static CurrentUser from(JwtAuthenticationToken auth) {
        var jwt = auth.getToken();
        var roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .collect(Collectors.toUnmodifiableSet());
        String name = jwt.getClaimAsString("name");
        // Numéro pris en compte seulement s'il a été vérifié (connexion par code WhatsApp).
        String phone = Boolean.TRUE.equals(jwt.getClaimAsBoolean("phone_number_verified")) ? jwt.getClaimAsString("phone_number") : null;
        return new CurrentUser(UUID.fromString(jwt.getSubject()), jwt.getClaimAsString("email"), name, phone, roles);
    }
}
