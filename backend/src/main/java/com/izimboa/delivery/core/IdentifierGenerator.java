package com.izimboa.delivery.core;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Références lisibles, liens de suivi impossibles à deviner, codes de remise. */
@Component
class IdentifierGenerator {

    /** Sans 0/O, 1/I/L : se dicte au téléphone sans confusion. */
    private static final char[] ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();

    private final SecureRandom random = new SecureRandom();

    Delivery.Identifiers next() {
        return new Delivery.Identifiers(UUID.randomUUID(), reference(), trackingToken(), handoverCode());
    }

    private String reference() {
        var sb = new StringBuilder("IZ-");
        for (int i = 0; i < 6; i++) sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        return sb.toString();
    }

    private String trackingToken() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String handoverCode() {
        return "%04d".formatted(random.nextInt(10_000));
    }
}
