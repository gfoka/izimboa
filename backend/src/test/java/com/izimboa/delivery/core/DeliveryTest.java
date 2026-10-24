package com.izimboa.delivery.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.izimboa.delivery.DeliveryProblem;
import com.izimboa.delivery.pricing.Quote;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Règles des étapes, sans base de données. */
class DeliveryTest {

    static final UUID VENDEUR = UUID.randomUUID();
    static final UUID LIVREUR = UUID.randomUUID();
    static final UUID OPERATEUR = UUID.randomUUID();
    static final Instant T = Instant.parse("2026-10-24T10:00:00Z");

    static Delivery nouvelle(long cod) {
        var ids = new Delivery.Identifiers(UUID.randomUUID(), "IZ-ABC234", "jeton", "4821");
        var details = new Delivery.Details(new Delivery.Stop(4.05, 9.76, "Akwa", null), "+237699000001",
                new Delivery.Stop(4.06, 9.77, "Bonamoussadi", "Derrière la boulangerie"), "Awa", "+237699000002",
                "Chaussures", false, cod);
        return Delivery.create(ids, VENDEUR, null, details, new Quote(4000, false, 1500, 0, 0, 1500), T);
    }

    static Delivery enRoute() {
        var d = nouvelle(0);
        d.assign(LIVREUR, OPERATEUR, T);
        d.pickUp(LIVREUR, null, T);
        d.depart(LIVREUR, null, T);
        return d;
    }

    @Test
    void parcoursComplet() {
        var d = enRoute();
        d.deliver("4821", LIVREUR, new Delivery.Position(4.06, 9.77), T);
        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(d.getEvents()).extracting(DeliveryEvent::status).containsExactly(
                DeliveryStatus.CREATED, DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP,
                DeliveryStatus.IN_TRANSIT, DeliveryStatus.DELIVERED);
    }

    @Test
    void onNeSautePasDEtape() {
        var d = nouvelle(0);
        assertThatThrownBy(() -> d.pickUp(LIVREUR, null, T)).isInstanceOf(DeliveryProblem.class);
        assertThatThrownBy(() -> d.deliver("4821", LIVREUR, null, T)).isInstanceOf(DeliveryProblem.class);
        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.CREATED);
    }

    @Test
    void unCodeFauxEstCompteEtBloqueApresCinqEssais() {
        var d = enRoute();
        for (int i = 1; i <= 4; i++) {
            assertThatThrownBy(() -> d.deliver("0000", LIVREUR, null, T)).isInstanceOf(DeliveryProblem.class);
            assertThat(d.getHandoverFailures()).isEqualTo(i);
        }
        assertThatThrownBy(() -> d.deliver("0000", LIVREUR, null, T))
                .isInstanceOfSatisfying(DeliveryProblem.class, e ->
                        assertThat(e.getBody().getProperties()).containsEntry("code", "handover-locked"));
        // Même le bon code est refusé tant qu'un opérateur n'a pas débloqué.
        assertThatThrownBy(() -> d.deliver("4821", LIVREUR, null, T)).isInstanceOf(DeliveryProblem.class);
        d.unlockHandover(OPERATEUR, T);
        d.deliver("4821", LIVREUR, null, T);
        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void echecPuisNouvelleTentativePuisRetour() {
        var d = enRoute();
        d.fail("Destinataire absent", LIVREUR, null, T);
        d.retry(LIVREUR, null, T);
        assertThat(d.getDeliveryAttempts()).isEqualTo(2);
        d.fail("Toujours absent", LIVREUR, null, T);
        assertThatThrownBy(() -> d.retry(LIVREUR, null, T)).isInstanceOf(DeliveryProblem.class);
        d.returnToSender(750, LIVREUR, null, T);
        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.RETURNED);
        assertThat(d.getReturnFee()).isEqualTo(750);
    }

    @Test
    void annulationPossibleSeulementAvantLeRamassage() {
        var d = nouvelle(0);
        d.assign(LIVREUR, OPERATEUR, T);
        d.cancel("Client a changé d'avis", VENDEUR, T);
        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);

        var ramassee = nouvelle(0);
        ramassee.assign(LIVREUR, OPERATEUR, T);
        ramassee.pickUp(LIVREUR, null, T);
        assertThatThrownBy(() -> ramassee.cancel(null, VENDEUR, T)).isInstanceOf(DeliveryProblem.class);
    }

    @Test
    void lesFraisSontRetenusSurLEncaissementQuandIlSuffit() {
        var avecEncaissement = nouvelle(20_000);
        assertThat(avecEncaissement.getFeePayment()).isEqualTo(FeePayment.DEDUCTED_FROM_COD);
        assertThat(avecEncaissement.sellerPayout()).isEqualTo(18_500);

        var sansEncaissement = nouvelle(0);
        assertThat(sansEncaissement.getFeePayment()).isEqualTo(FeePayment.PREPAID);
        assertThat(sansEncaissement.sellerPayout()).isZero();
    }

    @Test
    void uneCourseSurDevisNePeutPasEtreCreee() {
        var ids = new Delivery.Identifiers(UUID.randomUUID(), "IZ-ABC235", "jeton2", "1111");
        var details = new Delivery.Details(new Delivery.Stop(4.05, 9.76, "A", null), "+237699000001",
                new Delivery.Stop(4.2, 9.9, "B", null), "Awa", "+237699000002", null, false, 0);
        assertThatThrownBy(() -> Delivery.create(ids, VENDEUR, null, details, new Quote(20_000, true, 0, 0, 0, 0), T))
                .isInstanceOf(DeliveryProblem.class);
    }
}
