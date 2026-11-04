package com.izimboa.common.whatsapp;

import com.izimboa.common.WhatsAppMessages;
import com.izimboa.common.WhatsAppMessages.Result;
import com.izimboa.common.WhatsAppMessages.Status;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * API WhatsApp Cloud de Meta si le jeton WHATSAPP_TOKEN est fourni ; sinon rien n'est envoyé
 * (développement, tests) et l'appelant le sait (statut DISABLED).
 */
@Configuration
class WhatsAppConfig {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppConfig.class);

    @Bean
    WhatsAppMessages whatsAppMessages(@Value("${izimboa.whatsapp.token:}") String token,
                                      @Value("${izimboa.whatsapp.phone-number-id}") String phoneNumberId,
                                      @Value("${izimboa.whatsapp.api-base:https://graph.facebook.com/v21.0}") String apiBase) {
        if (token == null || token.isBlank()) {
            log.warn("WhatsApp désactivé : WHATSAPP_TOKEN absent (les notifications ne sont pas envoyées).");
            return new WhatsAppMessages() {
                @Override public Result send(WhatsAppMessages.Template t) {
                    log.info("WhatsApp (non envoyé) : modèle {} à {}", t.name(), mask(t.to()));
                    return Result.disabled();
                }

                @Override public Result sendText(String to, String body) {
                    log.info("WhatsApp (non envoyé) : texte à {}", mask(to));
                    return Result.disabled();
                }
            };
        }
        var timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(5));
        timeouts.setReadTimeout(Duration.ofSeconds(15));
        RestClient http = RestClient.builder().baseUrl(apiBase).requestFactory(timeouts)
                .defaultHeader("Authorization", "Bearer " + token).build();
        return new WhatsAppMessages() {
            @Override public Result send(WhatsAppMessages.Template t) {
                return post(http, phoneNumberId, payload(t), "modèle " + t.name(), t.to());
            }

            @Override public Result sendText(String to, String body) {
                var payload = new LinkedHashMap<String, Object>();
                payload.put("messaging_product", "whatsapp");
                payload.put("to", to.replace("+", ""));
                payload.put("type", "text");
                payload.put("text", Map.of("body", body, "preview_url", false));
                return post(http, phoneNumberId, payload, "texte", to);
            }
        };
    }

    private static Result post(RestClient http, String phoneNumberId, Map<String, Object> body, String what, String to) {
        try {
            Map<?, ?> res = http.post().uri("/{id}/messages", phoneNumberId).contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(Map.class);
            String id = messageId(res);
            log.info("WhatsApp envoyé : {} à {} ({})", what, mask(to), id);
            return new Result(Status.SENT, id, null);
        } catch (RestClientResponseException e) {
            String error = shorten(e.getStatusCode().value() + " " + e.getResponseBodyAsString());
            log.error("WhatsApp refusé : {} à {} : {}", what, mask(to), error);
            return new Result(Status.FAILED, null, error);
        } catch (Exception e) {
            log.error("WhatsApp en échec : {} à {} : {}", what, mask(to), e.getMessage());
            return new Result(Status.FAILED, null, shorten(String.valueOf(e.getMessage())));
        }
    }

    /** Corps attendu par l'API Cloud pour un message à modèle (texte à variables, bouton lien facultatif). */
    static Map<String, Object> payload(WhatsAppMessages.Template t) {
        List<Map<String, Object>> components = new ArrayList<>();
        if (!t.body().isEmpty()) {
            components.add(Map.of("type", "body",
                    "parameters", t.body().stream().map(v -> Map.<String, Object>of("type", "text", "text", v)).toList()));
        }
        if (t.urlSuffix() != null) {
            components.add(Map.of("type", "button", "sub_type", "url", "index", "0",
                    "parameters", List.of(Map.of("type", "text", "text", t.urlSuffix()))));
        }
        var template = new LinkedHashMap<String, Object>();
        template.put("name", t.name());
        template.put("language", Map.of("code", t.language()));
        template.put("components", components);
        var body = new LinkedHashMap<String, Object>();
        body.put("messaging_product", "whatsapp");
        body.put("to", t.to().replace("+", ""));
        body.put("type", "template");
        body.put("template", template);
        return body;
    }

    private static String messageId(Map<?, ?> res) {
        if (res != null && res.get("messages") instanceof List<?> l && !l.isEmpty() && l.getFirst() instanceof Map<?, ?> m) {
            return String.valueOf(m.get("id"));
        }
        return null;
    }

    private static String shorten(String s) {
        return s.length() > 300 ? s.substring(0, 300) : s;
    }

    /** Le journal ne garde pas le numéro complet. */
    static String mask(String phone) {
        return phone == null || phone.length() < 6 ? "?" : phone.substring(0, phone.length() - 4) + "****";
    }
}
