package com.izimboa.delivery.notify;

import com.izimboa.common.WhatsAppMessages.Template;
import java.util.List;

/** Le modèle WhatsApp et ses valeurs pour chaque étape (textes dans infra/whatsapp/templates). */
final class RecipientMessages {

    static final String LANGUAGE = "fr";
    static final String CREATED = "izimboa_livraison_creee";
    static final String CREATED_ASK_DROPOFF = "izimboa_livraison_lieu";
    static final String IN_TRANSIT = "izimboa_livraison_en_route";
    static final String DELIVERED = "izimboa_colis_livre";

    private RecipientMessages() {}

    static Template of(RecipientNotice n) {
        String name = text(n.recipientName(), "cher client", 40);
        String pkg = n.packageDescription() == null || n.packageDescription().isBlank()
                ? "réf. " + n.reference() : text(n.packageDescription(), "réf. " + n.reference(), 60);
        String amount = amount(n.amountToPay());
        return switch (n.kind()) {
            case CREATED -> n.askDropoff()
                    ? new Template(n.recipientPhone(), CREATED_ASK_DROPOFF, LANGUAGE,
                            List.of(name, pkg, text(n.area(), "indiqué par le vendeur", 60), amount), n.trackingToken())
                    : new Template(n.recipientPhone(), CREATED, LANGUAGE,
                            List.of(name, pkg, n.reference(), amount), n.trackingToken());
            case IN_TRANSIT -> new Template(n.recipientPhone(), IN_TRANSIT, LANGUAGE, List.of(name, pkg, amount), n.trackingToken());
            case DELIVERED -> new Template(n.recipientPhone(), DELIVERED, LANGUAGE, List.of(name, pkg), null);
        };
    }

    /** 15000 → « 15 000 FCFA » ; rien à payer → phrase explicite. */
    static String amount(long xaf) {
        if (xaf <= 0) return "rien, c'est déjà réglé";
        var digits = Long.toString(xaf);
        var sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (digits.length() - i) % 3 == 0) sb.append(' ');
            sb.append(digits.charAt(i));
        }
        return sb + " FCFA";
    }

    /** Meta refuse les retours à la ligne, tabulations et longues suites d'espaces dans les variables. */
    static String text(String value, String fallback, int max) {
        if (value == null) return fallback;
        String s = value.replaceAll("\\s+", " ").strip();
        if (s.isEmpty()) return fallback;
        return s.length() > max ? s.substring(0, max - 1).strip() + "…" : s;
    }
}
