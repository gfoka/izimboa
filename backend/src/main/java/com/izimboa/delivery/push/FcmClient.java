package com.izimboa.delivery.push;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Firebase Cloud Messaging (API HTTP v1), avec le compte de service du projet Firebase « izimboa ».
 * Le jeton d'accès Google est obtenu en signant un JWT avec la clé du compte de service, puis renouvelé avant expiration.
 * Sans compte de service (développement, tests), rien n'est envoyé.
 */
class FcmClient {

    private static final Logger log = LoggerFactory.getLogger(FcmClient.class);
    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

    enum Outcome { SENT, UNREGISTERED, FAILED, DISABLED }

    private final String projectId;
    private final String clientEmail;
    private final String tokenUri;
    private final PrivateKey key;
    private final RestClient http;
    private final ObjectMapper json;
    private final Clock clock;
    private String accessToken;
    private Instant accessUntil = Instant.EPOCH;

    private FcmClient(String projectId, String clientEmail, String tokenUri, PrivateKey key, ObjectMapper json, Clock clock) {
        this.projectId = projectId;
        this.clientEmail = clientEmail;
        this.tokenUri = tokenUri;
        this.key = key;
        this.json = json;
        this.clock = clock;
        var timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(5));
        timeouts.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder().requestFactory(timeouts).build();
    }

    static FcmClient disabled() {
        return new FcmClient(null, null, null, null, null, null);
    }

    /** @param serviceAccount contenu JSON du compte de service, tel quel ou encodé en base64 (une ligne dans .env). */
    static FcmClient from(String serviceAccount, ObjectMapper json, Clock clock) {
        if (serviceAccount == null || serviceAccount.isBlank()) return disabled();
        try {
            String raw = serviceAccount.strip();
            if (!raw.startsWith("{")) raw = new String(Base64.getMimeDecoder().decode(raw), StandardCharsets.UTF_8);
            JsonNode sa = json.readTree(raw);
            String pem = sa.path("private_key").asText()
                    .replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
            PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
            String tokenUri = sa.path("token_uri").asText("https://oauth2.googleapis.com/token");
            var client = new FcmClient(sa.path("project_id").asText(), sa.path("client_email").asText(), tokenUri, key, json, clock);
            log.info("Notifications push actives (projet Firebase {}).", client.projectId);
            return client;
        } catch (Exception e) {
            log.error("Compte de service Firebase illisible : notifications push désactivées ({}).", e.getClass().getSimpleName());
            return disabled();
        }
    }

    boolean enabled() { return key != null; }

    /** Message de données, priorité haute : réveille l'application même en veille (Doze). */
    Outcome send(String token, Map<String, String> data, int ttlSeconds) {
        if (!enabled()) return Outcome.DISABLED;
        var android = new LinkedHashMap<String, Object>();
        android.put("priority", "HIGH");
        android.put("ttl", Math.max(1, ttlSeconds) + "s");
        var message = Map.of("message", Map.of("token", token, "data", data, "android", android));
        try {
            String bearer = accessToken();
            http.post().uri("https://fcm.googleapis.com/v1/projects/{p}/messages:send", projectId)
                    .headers(h -> h.setBearerAuth(bearer)).contentType(MediaType.APPLICATION_JSON)
                    .body(message).retrieve().toBodilessEntity();
            return Outcome.SENT;
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 404 || body.contains("UNREGISTERED")
                    || (e.getStatusCode().value() == 400 && body.contains("registration token"))) {
                return Outcome.UNREGISTERED;
            }
            log.warn("Push refusé par Firebase : {} {}", e.getStatusCode().value(), body.length() > 300 ? body.substring(0, 300) : body);
            return Outcome.FAILED;
        } catch (Exception e) {
            log.warn("Push non envoyé : {}", e.toString());
            return Outcome.FAILED;
        }
    }

    private synchronized String accessToken() throws Exception {
        Instant now = clock.instant();
        if (accessToken != null && now.isBefore(accessUntil)) return accessToken;
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        form.add("assertion", assertion(now));
        JsonNode res = json.readTree(http.post().uri(tokenUri).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().body(String.class));
        accessToken = res.path("access_token").asText();
        accessUntil = now.plusSeconds(Math.max(60, res.path("expires_in").asLong(3600) - 120));
        return accessToken;
    }

    private String assertion(Instant now) throws Exception {
        var b64 = Base64.getUrlEncoder().withoutPadding();
        String header = b64.encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        var claims = new LinkedHashMap<String, Object>();
        claims.put("iss", clientEmail);
        claims.put("scope", SCOPE);
        claims.put("aud", tokenUri);
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.getEpochSecond() + 3600);
        String payload = b64.encodeToString(json.writeValueAsBytes(claims));
        var signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(key);
        signer.update((header + "." + payload).getBytes(StandardCharsets.US_ASCII));
        return header + "." + payload + "." + b64.encodeToString(signer.sign());
    }
}
