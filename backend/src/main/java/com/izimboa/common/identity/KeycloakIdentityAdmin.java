package com.izimboa.common.identity;

import com.izimboa.common.IdentityAdmin;
import com.izimboa.common.Phones;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/** API d'administration de Keycloak, avec le jeton du compte de service (renouvelé avant expiration). */
class KeycloakIdentityAdmin implements IdentityAdmin {

    private static final Logger log = LoggerFactory.getLogger(KeycloakIdentityAdmin.class);

    private final RestClient http;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private String token;
    private Instant tokenUntil = Instant.EPOCH;

    KeycloakIdentityAdmin(String baseUrl, String realm, String clientId, String clientSecret) {
        var timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(5));
        timeouts.setReadTimeout(Duration.ofSeconds(15));
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(timeouts).build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public boolean enabled() { return true; }

    @Override
    public Optional<Account> find(UUID id) {
        try {
            return Optional.ofNullable(account(get("/users/" + id, Map.class)));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Account> findByLogin(String login) {
        String s = login == null ? "" : login.strip();
        if (s.isEmpty()) return Optional.empty();
        List<Map<?, ?>> found = new ArrayList<>();
        var phone = Phones.normalize(s);
        if (phone.isPresent()) {
            found.addAll(list("/users?exact=true&briefRepresentation=false&q=phoneNumber:" + phone.get()));
            if (found.isEmpty()) found.addAll(list("/users?exact=true&briefRepresentation=false&username=tel-" + phone.get().substring(1)));
        } else if (s.contains("@")) {
            found.addAll(list("/users?exact=true&briefRepresentation=false&email=" + s));
        } else {
            found.addAll(list("/users?exact=true&briefRepresentation=false&username=" + s));
        }
        return found.stream().findFirst().map(KeycloakIdentityAdmin::account);
    }

    @Override
    public List<Account> withRole(String role) {
        return list("/roles/" + role + "/users?briefRepresentation=false&first=0&max=500").stream()
                .map(KeycloakIdentityAdmin::account).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateName(UUID id, String firstName, String lastName, String email) {
        Map<String, Object> user = new LinkedHashMap<>((Map<String, Object>) get("/users/" + id, Map.class));
        boolean hasEmail = user.get("email") != null && !String.valueOf(user.get("email")).isBlank();
        boolean newEmail = email != null && !email.isBlank();
        // Le realm partagé par tous les produits exige un e-mail pour toute mise à jour d'un compte. Un livreur
        // inscrit par WhatsApp peut ne pas en avoir (e-mail facultatif dans Delivery) : son nom reste alors dans
        // le dossier Delivery seulement, sans changer les règles de Keycloak pour les autres produits.
        if (!hasEmail && !newEmail) {
            log.info("Compte {} sans e-mail : nom gardé dans Izimboa seulement (Keycloak exige un e-mail pour le modifier).", id);
            return;
        }
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        if (newEmail) user.put("email", email.strip().toLowerCase());
        try {
            http.put().uri(admin("/users/" + id)).headers(h -> h.setBearerAuth(token()))
                    .contentType(MediaType.APPLICATION_JSON).body(user).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new IdentityException(409, "Cette adresse e-mail est déjà utilisée par un autre compte.");
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Keycloak refuse la mise à jour du compte {} : {}", id, e.getResponseBodyAsString());
            String body = e.getResponseBodyAsString();
            throw new IdentityException(400, body.contains("\"email\"") ? "Adresse e-mail refusée : vérifiez la saisie."
                    : "Prénom ou nom refusé : évitez les caractères spéciaux.");
        }
    }

    @Override
    public void addRole(UUID id, String role) {
        var rep = get("/roles/" + role, Map.class);
        http.post().uri(admin("/users/" + id + "/role-mappings/realm")).headers(h -> h.setBearerAuth(token()))
                .contentType(MediaType.APPLICATION_JSON).body(List.of(rep)).retrieve().toBodilessEntity();
        log.info("Rôle « {} » donné au compte {}", role, id);
    }

    @Override
    public void removeRole(UUID id, String role) {
        var rep = get("/roles/" + role, Map.class);
        http.method(org.springframework.http.HttpMethod.DELETE).uri(admin("/users/" + id + "/role-mappings/realm"))
                .headers(h -> h.setBearerAuth(token())).contentType(MediaType.APPLICATION_JSON).body(List.of(rep))
                .retrieve().toBodilessEntity();
        log.info("Rôle « {} » retiré au compte {}", role, id);
    }

    // ---- Interne ----

    private String admin(String path) {
        return "/admin/realms/" + realm + path;
    }

    private <T> T get(String path, Class<T> type) {
        return http.get().uri(admin(path)).headers(h -> h.setBearerAuth(token())).retrieve().body(type);
    }

    @SuppressWarnings("unchecked")
    private List<Map<?, ?>> list(String path) {
        List<Map<?, ?>> res = http.get().uri(admin(path)).headers(h -> h.setBearerAuth(token())).retrieve().body(List.class);
        return res == null ? List.of() : res;
    }

    private synchronized String token() {
        if (token != null && Instant.now().isBefore(tokenUntil)) return token;
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        Map<?, ?> res = http.post().uri("/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Map.class);
        if (res == null || res.get("access_token") == null) throw new IdentityException(503, "Gestion des comptes indisponible.");
        token = String.valueOf(res.get("access_token"));
        long ttl = res.get("expires_in") instanceof Number n ? n.longValue() : 60;
        tokenUntil = Instant.now().plusSeconds(Math.max(10, ttl - 30));
        return token;
    }

    static Account account(Map<?, ?> u) {
        if (u == null) return null;
        String phone = null;
        if (u.get("attributes") instanceof Map<?, ?> attrs && attrs.get("phoneNumber") instanceof List<?> l && !l.isEmpty()) {
            phone = String.valueOf(l.getFirst());
        }
        return new Account(UUID.fromString(String.valueOf(u.get("id"))), str(u.get("username")), str(u.get("firstName")),
                str(u.get("lastName")), str(u.get("email")), phone, !Boolean.FALSE.equals(u.get("enabled")));
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
