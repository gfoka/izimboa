package com.izimboa.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtConverterTest {

    private static Jwt jwt(Map<String, Object> claims) {
        var b = Jwt.withTokenValue("t").header("alg", "RS256").subject("0d3c7b8e-1111-4a5b-9c2d-123456789abc")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60));
        claims.forEach(b::claim);
        return b.build();
    }

    @Test
    void lesRolesDuRealmDeviennentDesAutorites() {
        var auth = new KeycloakJwtConverter().convert(jwt(Map.of("realm_access", Map.of("roles", List.of("client", "livreur")))));
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_client", "ROLE_livreur");
        assertThat(auth.getName()).isEqualTo("0d3c7b8e-1111-4a5b-9c2d-123456789abc");
    }

    @Test
    void unJetonSansRolesNaAucuneAutorite() {
        var auth = new KeycloakJwtConverter().convert(jwt(Map.of("email", "a@b.c")));
        assertThat(auth.getAuthorities()).isEmpty();
    }
}
