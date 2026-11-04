package com.izimboa.delivery.notify;

import com.fasterxml.jackson.databind.JsonNode;
import com.izimboa.common.WhatsAppMessages;
import com.izimboa.common.mail.Mailer;
import com.izimboa.delivery.core.DeliveryLookup;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ce que Meta nous envoie pour le numéro Izimboa (décision 75) :
 * <ul>
 *   <li>les accusés de nos messages (distribué, lu, échec) ;</li>
 *   <li>les messages des clients : rattachés à leur livraison, transmis par e-mail au support, avec une réponse
 *       automatique (une toutes les 6 heures au plus par numéro).</li>
 * </ul>
 */
@Service
class WhatsAppInbox {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppInbox.class);
    static final Duration AUTO_REPLY_EVERY = Duration.ofHours(6);
    static final Duration RECENT_DELIVERIES = Duration.ofDays(30);
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm").withZone(ZoneId.of("Africa/Douala"));

    private final InboundMessageRepository inbound;
    private final RecipientNotificationRepository notifications;
    private final DeliveryLookup deliveries;
    private final WhatsAppMessages whatsApp;
    private final Mailer mailer;
    private final Clock clock;
    private final String supportEmail;
    private final String phoneNumberId;

    WhatsAppInbox(InboundMessageRepository inbound, RecipientNotificationRepository notifications, DeliveryLookup deliveries,
                  WhatsAppMessages whatsApp, Mailer mailer, Clock clock,
                  @Value("${izimboa.mail.support}") String supportEmail,
                  @Value("${izimboa.whatsapp.phone-number-id}") String phoneNumberId) {
        this.inbound = inbound;
        this.notifications = notifications;
        this.deliveries = deliveries;
        this.whatsApp = whatsApp;
        this.mailer = mailer;
        this.clock = clock;
        this.supportEmail = supportEmail;
        this.phoneNumberId = phoneNumberId;
    }

    /** Un envoi du webhook (déjà authentifié par sa signature). Ne lève pas d'exception : Meta recommencerait. */
    void handle(JsonNode payload) {
        for (JsonNode entry : payload.path("entry")) {
            for (JsonNode change : entry.path("changes")) {
                JsonNode value = change.path("value");
                String to = value.path("metadata").path("phone_number_id").asText("");
                if (!to.isEmpty() && !to.equals(phoneNumberId)) continue;   // autre numéro du même compte
                for (JsonNode s : value.path("statuses")) safely(() -> status(s));
                String name = value.path("contacts").path(0).path("profile").path("name").asText(null);
                for (JsonNode m : value.path("messages")) safely(() -> message(m, name));
            }
        }
    }

    @Transactional
    void status(JsonNode s) {
        String id = s.path("id").asText("");
        String status = s.path("status").asText("");
        if (id.isEmpty() || status.isEmpty()) return;
        JsonNode err = s.path("errors").path(0);
        String error = err.isMissingNode() ? null
                : (err.path("code").asText("") + " " + err.path("error_data").path("details").asText(err.path("title").asText(""))).strip();
        if (error != null && error.length() > 300) error = error.substring(0, 300);
        Instant at = epoch(s.path("timestamp").asText(null));
        String e = error;
        notifications.findFirstByMessageId(id).ifPresent(n -> {
            n.whatsAppStatus(status, e, at);
            notifications.save(n);
        });
    }

    void message(JsonNode m, String profileName) {
        String waId = m.path("id").asText("");
        if (waId.isEmpty() || inbound.existsByWaMessageId(waId)) return;   // Meta renvoie parfois le même message
        String phone = "+" + m.path("from").asText("").replaceAll("\\D", "");
        String type = m.path("type").asText("unknown");
        String context = m.path("context").path("id").asText(null);
        var msg = new InboundMessage(waId, phone, profileName, type, text(m, type), context, epoch(m.path("timestamp").asText(null)));

        // De quelle livraison parle-t-il ? D'abord le message auquel il répond, sinon son numéro.
        List<DeliveryLookup.DeliveryRef> candidates = List.of();
        if (context != null) {
            candidates = notifications.findFirstByMessageId(context)
                    .flatMap(n -> deliveries.byId(n.getDeliveryId())).map(List::of).orElse(List.of());
        }
        if (candidates.isEmpty()) candidates = deliveries.recentForRecipient(phone, clock.instant().minus(RECENT_DELIVERIES));
        if (candidates.size() == 1) msg.linkTo(candidates.getFirst().id());

        try {
            inbound.saveAndFlush(msg);
        } catch (DataIntegrityViolationException duplicate) {
            return;
        }
        if (type.equals("reaction")) return;   // un pouce levé sur « colis livré » : rien à traiter

        mailer.send(new Mailer.Message(supportEmail, "Support Izimboa", null, subject(msg, candidates), body(msg, candidates)));
        msg.forwarded(clock.instant());

        boolean recentlyAnswered = inbound.existsByFromPhoneAndAutoRepliedTrueAndReceivedAtAfter(phone,
                clock.instant().minus(AUTO_REPLY_EVERY));
        if (!recentlyAnswered) {
            String reply = candidates.size() == 1
                    ? "Merci, nous avons bien reçu votre message concernant la livraison " + candidates.getFirst().reference()
                      + ". Notre équipe vous répond rapidement."
                    : "Merci pour votre message. Notre équipe vous répond rapidement. Pour faire livrer un colis : https://delivery.izimboa.com";
            if (whatsApp.sendText(phone, reply).status() == WhatsAppMessages.Status.SENT) msg.autoReplied();
        }
        inbound.save(msg);
    }

    // ---- Interne ----

    static String text(JsonNode m, String type) {
        String t = switch (type) {
            case "text" -> m.path("text").path("body").asText("");
            case "button" -> m.path("button").path("text").asText("");
            case "interactive" -> m.path("interactive").path("button_reply").path("title")
                    .asText(m.path("interactive").path("list_reply").path("title").asText(""));
            case "location" -> "Position partagée : https://www.google.com/maps?q=" + m.path("location").path("latitude").asText()
                    + "," + m.path("location").path("longitude").asText()
                    + (m.path("location").has("name") ? " (" + m.path("location").path("name").asText() + ")" : "");
            case "reaction" -> "Réaction " + m.path("reaction").path("emoji").asText("");
            case "image", "video", "document", "audio", "sticker" -> "[" + switch (type) {
                    case "image" -> "Photo"; case "video" -> "Vidéo"; case "document" -> "Document";
                    case "audio" -> "Message vocal"; default -> "Autocollant"; } + " : à consulter sur le téléphone du client]"
                    + (m.path(type).has("caption") ? " " + m.path(type).path("caption").asText() : "");
            default -> "[Message de type « " + type + " »]";
        };
        return t.length() > 4000 ? t.substring(0, 4000) : t;
    }

    private static String subject(InboundMessage msg, List<DeliveryLookup.DeliveryRef> candidates) {
        String who = msg.getProfileName() == null ? msg.getFromPhone() : msg.getProfileName() + " (" + msg.getFromPhone() + ")";
        String about = candidates.size() == 1 ? candidates.getFirst().reference()
                : candidates.isEmpty() ? "aucune livraison trouvée" : candidates.size() + " livraisons possibles";
        return "[WhatsApp Izimboa] " + who + " : " + about;
    }

    private static String body(InboundMessage msg, List<DeliveryLookup.DeliveryRef> candidates) {
        var sb = new StringBuilder();
        sb.append("Message reçu le ").append(WHEN.format(msg.getReceivedAt())).append(" sur le numéro WhatsApp izimboa.\n\n");
        sb.append("De : ").append(msg.getProfileName() == null ? "" : msg.getProfileName() + " ").append(msg.getFromPhone()).append('\n');
        if (msg.getContextMessageId() != null) sb.append("En réponse à un message d'Izimboa.\n");
        sb.append("\n").append(msg.getBody()).append("\n\n");
        if (candidates.isEmpty()) {
            sb.append("Aucune livraison récente pour ce numéro (futur client, mauvais numéro, ou client qui écrit depuis un autre numéro).\n");
        } else {
            sb.append(candidates.size() == 1 ? "Livraison concernée :\n" : "Livraisons récentes de ce numéro :\n");
            for (var d : candidates) {
                sb.append("- ").append(d.reference()).append(" (").append(statusFr(d.status())).append(")")
                        .append(d.packageDescription() == null ? "" : ", " + d.packageDescription())
                        .append(" : https://delivery.izimboa.com/livraison/?id=").append(d.id()).append('\n');
            }
        }
        sb.append("\nPour lui répondre : appelez-le ou écrivez-lui sur WhatsApp, https://wa.me/").append(msg.getFromPhone().substring(1)).append('\n');
        sb.append("Il a reçu une réponse automatique lui disant que l'équipe va le recontacter (une fois toutes les 6 heures au plus).\n");
        return sb.toString();
    }

    private static String statusFr(String s) {
        return switch (s) {
            case "CREATED" -> "créée"; case "ASSIGNED" -> "livreur attribué"; case "PICKED_UP" -> "ramassée";
            case "IN_TRANSIT" -> "en route"; case "DELIVERED" -> "livrée"; case "FAILED" -> "échec de remise";
            case "RETURNED" -> "retournée au vendeur"; case "CANCELLED" -> "annulée"; default -> s;
        };
    }

    private Instant epoch(String seconds) {
        try {
            return seconds == null ? clock.instant() : Instant.ofEpochSecond(Long.parseLong(seconds));
        } catch (NumberFormatException e) {
            return clock.instant();
        }
    }

    private static void safely(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            log.error("Webhook WhatsApp : élément ignoré ({})", e.getMessage());
        }
    }
}
