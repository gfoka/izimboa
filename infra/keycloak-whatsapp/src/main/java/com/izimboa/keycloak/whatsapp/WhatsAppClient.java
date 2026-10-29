package com.izimboa.keycloak.whatsapp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.jboss.logging.Logger;

/**
 * Envoi du code par l'API WhatsApp Cloud de Meta, avec le modèle d'authentification « izimboa_code »
 * (bouton « Copier le code »). Réglages par variables d'environnement du conteneur Keycloak :
 * WHATSAPP_TOKEN (jeton de l'utilisateur système), WHATSAPP_PHONE_NUMBER_ID, WHATSAPP_CODE_TEMPLATE,
 * WHATSAPP_API_BASE (tests uniquement).
 */
public class WhatsAppClient {

    private static final Logger LOG = Logger.getLogger(WhatsAppClient.class);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final String apiBase;
    private final String token;
    private final String phoneNumberId;
    private final String template;

    public WhatsAppClient(String apiBase, String token, String phoneNumberId, String template) {
        this.apiBase = apiBase;
        this.token = token;
        this.phoneNumberId = phoneNumberId;
        this.template = template;
    }

    public static WhatsAppClient fromEnvironment() {
        return new WhatsAppClient(
                env("WHATSAPP_API_BASE", "https://graph.facebook.com/v21.0"),
                env("WHATSAPP_TOKEN", ""),
                env("WHATSAPP_PHONE_NUMBER_ID", "112115108576086"),
                env("WHATSAPP_CODE_TEMPLATE", "izimboa_code"));
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isBlank() ? fallback : v.strip();
    }

    public boolean configured() {
        return !token.isEmpty();
    }

    /** Envoie le code ; renvoie {@code true} si Meta a accepté le message. Ne journalise jamais le code ni le jeton. */
    public boolean sendCode(String e164, String code, String language) {
        if (!configured()) {
            LOG.warn("WHATSAPP_TOKEN absent : code WhatsApp non envoyé.");
            return false;
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiBase + "/" + phoneNumberId + "/messages"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload(e164, code, language)))
                .build();
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 == 2) return true;
            String body = response.body() == null ? "" : response.body();
            LOG.warnf("WhatsApp a refusé le code pour %s : HTTP %d %s",
                    PhoneNumbers.mask(e164), response.statusCode(), body.substring(0, Math.min(300, body.length())));
            return false;
        } catch (IOException e) {
            LOG.warnf("WhatsApp injoignable : %s", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** Corps du message : le code va dans le texte et dans le bouton « Copier le code ». */
    String payload(String e164, String code, String language) {
        String to = e164.startsWith("+") ? e164.substring(1) : e164;
        String lang = "en".equals(language) ? "en" : "fr";
        return """
                {"messaging_product":"whatsapp","recipient_type":"individual","to":"%s","type":"template",\
                "template":{"name":"%s","language":{"code":"%s"},"components":[\
                {"type":"body","parameters":[{"type":"text","text":"%s"}]},\
                {"type":"button","sub_type":"url","index":"0","parameters":[{"type":"text","text":"%s"}]}]}}"""
                .formatted(to, template, lang, code, code);
    }
}
