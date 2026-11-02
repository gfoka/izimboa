package com.izimboa.delivery.notify;

import static org.assertj.core.api.Assertions.assertThat;

import com.izimboa.delivery.notify.RecipientNotice.Kind;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipientMessagesTest {

    static RecipientNotice notice(Kind kind, String pkg, long amount, boolean ask) {
        return new RecipientNotice(UUID.randomUUID(), kind, "+237699000002", "Awa  Ngo\n", pkg, "IZ-4KQ7MZ", amount,
                "Bonamoussadi, carrefour Kotto", ask, "tok123");
    }

    @Test
    void creationAvecLieuFixeOuLieuADonner() {
        var fixed = RecipientMessages.of(notice(Kind.CREATED, "Chaussures", 15000, false));
        assertThat(fixed.name()).isEqualTo("izimboa_livraison_creee");
        assertThat(fixed.body()).containsExactly("Awa Ngo", "Chaussures", "IZ-4KQ7MZ", "15 000 FCFA");
        assertThat(fixed.urlSuffix()).isEqualTo("tok123");
        assertThat(fixed.to()).isEqualTo("+237699000002");

        var ask = RecipientMessages.of(notice(Kind.CREATED, null, 0, true));
        assertThat(ask.name()).isEqualTo("izimboa_livraison_lieu");
        assertThat(ask.body()).containsExactly("Awa Ngo", "réf. IZ-4KQ7MZ", "Bonamoussadi, carrefour Kotto", "rien, c'est déjà réglé");
    }

    @Test
    void enRouteEtLivre() {
        var route = RecipientMessages.of(notice(Kind.IN_TRANSIT, "Chaussures", 2500, false));
        assertThat(route.name()).isEqualTo("izimboa_livraison_en_route");
        assertThat(route.body()).isEqualTo(List.of("Awa Ngo", "Chaussures", "2 500 FCFA"));
        var done = RecipientMessages.of(notice(Kind.DELIVERED, "Chaussures", 2500, false));
        assertThat(done.name()).isEqualTo("izimboa_colis_livre");
        assertThat(done.urlSuffix()).isNull();
    }

    @Test
    void montantsEtTextesNettoyes() {
        assertThat(RecipientMessages.amount(1000)).isEqualTo("1 000 FCFA");
        assertThat(RecipientMessages.amount(1234567)).isEqualTo("1 234 567 FCFA");
        assertThat(RecipientMessages.amount(500)).isEqualTo("500 FCFA");
        assertThat(RecipientMessages.text("  \t ", "défaut", 10)).isEqualTo("défaut");
        assertThat(RecipientMessages.text("a\tb\n\nc", "x", 10)).isEqualTo("a b c");
        assertThat(RecipientMessages.text("x".repeat(80), "", 60)).hasSize(60).endsWith("…");
    }
}
