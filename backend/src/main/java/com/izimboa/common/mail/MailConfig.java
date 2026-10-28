package com.izimboa.common.mail;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Brevo si la clé BREVO_API_KEY est fournie, sinon les e-mails sont seulement inscrits au journal
 * (développement, ou tant que le compte Brevo n'est pas configuré : rien ne se perd, tout est en base).
 */
@Configuration
class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    Mailer mailer(@Value("${izimboa.mail.brevo-api-key:}") String apiKey,
                  @Value("${izimboa.mail.from}") String from,
                  @Value("${izimboa.mail.from-name}") String fromName) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("E-mails désactivés : BREVO_API_KEY absente (les messages sont seulement inscrits au journal).");
            return m -> log.info("E-mail (non envoyé) à {} : {}", m.to(), m.subject());
        }
        RestClient http = RestClient.builder().baseUrl("https://api.brevo.com/v3").defaultHeader("api-key", apiKey).build();
        return m -> {
            try {
                var body = new java.util.LinkedHashMap<String, Object>();
                body.put("sender", Map.of("email", from, "name", fromName));
                body.put("to", List.of(m.toName() == null ? Map.of("email", m.to()) : Map.of("email", m.to(), "name", m.toName())));
                if (m.replyTo() != null) body.put("replyTo", Map.of("email", m.replyTo()));
                body.put("subject", m.subject());
                body.put("textContent", m.text());
                http.post().uri("/smtp/email").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();
                log.info("E-mail envoyé à {} : {}", m.to(), m.subject());
            } catch (Exception e) {
                log.error("Échec d'envoi d'e-mail à {} : {} ({})", m.to(), m.subject(), e.getMessage());
            }
        };
    }
}
