package com.izimboa.common.me;

import com.izimboa.common.security.CurrentUserResolver;
import com.izimboa.common.profile.UserProfileService;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Profil de la personne connectée : permet de vérifier la chaîne complète Keycloak → API → base. */
@RestController
@RequestMapping("/v1/me")
public class MeController {

    private final UserProfileService profiles;

    public MeController(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public MeResponse me(JwtAuthenticationToken auth) {
        var user = CurrentUserResolver.from(auth);
        var profile = profiles.touch(user.id());
        return new MeResponse(user.id(), user.email(), user.name(), user.phone(), user.roles(), profile.getFirstSeenAt());
    }

    public record MeResponse(UUID id, String email, String name, String phone, Set<String> roles, Instant memberSince) {}
}
