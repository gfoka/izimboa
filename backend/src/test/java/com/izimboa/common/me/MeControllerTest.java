package com.izimboa.common.me;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.izimboa.common.profile.UserProfile;
import com.izimboa.common.profile.UserProfileService;
import com.izimboa.common.security.KeycloakJwtConverter;
import com.izimboa.common.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
class MeControllerTest {

    private static final String SUB = "0d3c7b8e-1111-4a5b-9c2d-123456789abc";

    @Autowired MockMvc mvc;
    @MockitoBean UserProfileService profiles;

    @Test
    void sansJetonLaRequeteEstRefusee() throws Exception {
        mvc.perform(get("/v1/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void avecUnJetonKeycloakLeProfilEstRenvoye() throws Exception {
        when(profiles.touch(any())).thenReturn(new UserProfile(UUID.fromString(SUB), Instant.parse("2026-10-24T00:00:00Z")));

        mvc.perform(get("/v1/me").with(jwt()
                        .jwt(j -> j.subject(SUB).claim("email", "client@izimboa.local").claim("name", "Awa Client")
                                .claim("realm_access", Map.of("roles", List.of("client", "vendeur"))))
                        .authorities(KeycloakJwtConverter::authorities)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SUB))
                .andExpect(jsonPath("$.email").value("client@izimboa.local"))
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.memberSince").value("2026-10-24T00:00:00Z"));
    }
}
