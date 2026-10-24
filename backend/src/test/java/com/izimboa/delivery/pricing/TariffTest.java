package com.izimboa.delivery.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class TariffTest {

    /** Grille de départ (décision n° 30). */
    static final Tariff GRILLE = new Tariff(
            List.of(new Tariff.Tier(7000, 1500), new Tariff.Tier(3000, 1000), new Tariff.Tier(12000, 2000)),
            500, 150, 50);

    @Test
    void lePrixDependDuPalierDeDistance() {
        assertThat(GRILLE.quote(2_999, false, 0).basePrice()).isEqualTo(1000);
        assertThat(GRILLE.quote(3_000, false, 0).basePrice()).isEqualTo(1000);
        assertThat(GRILLE.quote(3_001, false, 0).basePrice()).isEqualTo(1500);
        assertThat(GRILLE.quote(12_000, false, 0).total()).isEqualTo(2000);
    }

    @Test
    void auDelaDeLaGrilleCestSurDevis() {
        var q = GRILLE.quote(12_001, true, 10_000);
        assertThat(q.onRequest()).isTrue();
        assertThat(q.total()).isZero();
    }

    @Test
    void expressEtCommissionSurEncaissement() {
        var q = GRILLE.quote(5_000, true, 15_000);
        assertThat(q.basePrice()).isEqualTo(1500);
        assertThat(q.expressSurcharge()).isEqualTo(500);
        assertThat(q.codCommission()).isEqualTo(225);          // 1,5 % de 15 000
        assertThat(q.total()).isEqualTo(2225);
        assertThat(q.courseFee()).isEqualTo(2000);
    }

    @Test
    void laCommissionEstArrondieAuFrancLePlusProche() {
        assertThat(GRILLE.codCommission(10_033)).isEqualTo(150);  // 150,495
        assertThat(GRILLE.codCommission(10_034)).isEqualTo(151);  // 150,51
    }

    @Test
    void leRetourCouteLaMoitieDeLaCourse() {
        assertThat(GRILLE.returnFee(1000)).isEqualTo(500);
        assertThat(GRILLE.returnFee(1500)).isEqualTo(750);
    }

    @Test
    void uneGrilleIncoherenteEstRefusee() {
        assertThatThrownBy(() -> new Tariff(List.of(), 0, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Tariff(List.of(new Tariff.Tier(3000, 1000), new Tariff.Tier(7000, 900)), 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Tariff(List.of(new Tariff.Tier(3000, 1000)), 0, 20_000, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
