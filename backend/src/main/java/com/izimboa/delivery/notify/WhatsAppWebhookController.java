package com.izimboa.delivery.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adresse donnée à Meta pour le numéro WhatsApp Izimboa (décision 75). Sans connexion : chaque envoi est signé
 * par Meta avec le secret de l'application (en-tête X-Hub-Signature-256), tout envoi mal signé est refusé.
 */
@RestController
class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final WhatsAppInbox inbox;
    private final String appSecret;
    private final String verifyToken;

    WhatsAppWebhookController(WhatsAppInbox inbox,
                              @Value("${izimboa.whatsapp.app-secret:}") String appSecret,
                              @Value("${izimboa.whatsapp.verify-token:}") String verifyToken) {
        this.inbox = inbox;
        this.appSecret = appSecret;
        this.verifyToken = verifyToken;
    }

    /** Vérification de l'adresse par Meta lors de l'abonnement. */
    @GetMapping(value = "/v1/whatsapp/webhook", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> verify(@RequestParam(name = "hub.mode", required = false) String mode,
                                  @RequestParam(name = "hub.verify_token", required = false) String token,
                                  @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if ("subscribe".equals(mode) && !verifyToken.isBlank() && constantTimeEquals(verifyToken, token) && challenge != null) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).build();
    }

    @PostMapping(value = "/v1/whatsapp/webhook", consumes = MediaType.ALL_VALUE)
    ResponseEntity<Void> receive(@RequestBody byte[] body,
                                 @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature) {
        if (appSecret.isBlank() || !validSignature(appSecret, body, signature)) {
            log.warn("Webhook WhatsApp refusé : signature absente ou invalide.");
            return ResponseEntity.status(403).build();
        }
        try {
            inbox.handle(JSON.readTree(body));
        } catch (Exception e) {
            log.error("Webhook WhatsApp illisible : {}", e.getMessage());
        }
        return ResponseEntity.ok().build();   // toujours 200 une fois authentifié : Meta n'a pas à renvoyer
    }

    static boolean validSignature(String secret, byte[] body, String header) {
        if (header == null || !header.startsWith("sha256=")) return false;
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(body);
            byte[] given = HexFormat.of().parseHex(header.substring(7).strip());
            return MessageDigest.isEqual(expected, given);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return b != null && MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
