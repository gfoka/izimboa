package com.izimboa.delivery.pricing;

import java.util.Comparator;
import java.util.List;

/**
 * Grille de prix de Delivery : prix par palier de distance, stable pour le vendeur
 * (ni la pluie ni les bouchons ne le changent, décision n° 31).
 * Objet pur, sans base de données : facile à tester.
 *
 * @param codCommissionBp commission sur l'argent encaissé, en points de base (150 = 1,5 %)
 */
public record Tariff(List<Tier> tiers, long expressSurcharge, int codCommissionBp, int returnFeePercent) {

    public record Tier(int maxDistanceMeters, long price) {}

    public Tariff {
        if (tiers.isEmpty()) throw new IllegalArgumentException("La grille doit avoir au moins un palier");
        tiers = tiers.stream().sorted(Comparator.comparingInt(Tier::maxDistanceMeters)).toList();
        for (int i = 1; i < tiers.size(); i++) {
            if (tiers.get(i).maxDistanceMeters() == tiers.get(i - 1).maxDistanceMeters()) {
                throw new IllegalArgumentException("Deux paliers ont la même distance");
            }
            if (tiers.get(i).price() < tiers.get(i - 1).price()) {
                throw new IllegalArgumentException("Un palier plus long ne peut pas coûter moins cher");
            }
        }
        if (tiers.getFirst().maxDistanceMeters() <= 0 || tiers.getFirst().price() <= 0) {
            throw new IllegalArgumentException("Distances et prix doivent être positifs");
        }
        if (expressSurcharge < 0 || codCommissionBp < 0 || codCommissionBp > 10_000
                || returnFeePercent < 0 || returnFeePercent > 100) {
            throw new IllegalArgumentException("Réglages hors limites");
        }
    }

    public Quote quote(int distanceMeters, boolean express, long codAmount) {
        if (codAmount < 0) throw new IllegalArgumentException("Montant à encaisser négatif");
        var tier = tiers.stream().filter(t -> distanceMeters <= t.maxDistanceMeters()).findFirst();
        if (tier.isEmpty()) {
            return Quote.onRequest(distanceMeters);
        }
        long base = tier.get().price();
        long expressFee = express ? expressSurcharge : 0;
        long commission = codCommission(codAmount);
        return new Quote(distanceMeters, false, base, expressFee, commission, base + expressFee + commission);
    }

    /** Commission arrondie au franc le plus proche. */
    long codCommission(long codAmount) {
        return (codAmount * codCommissionBp + 5_000) / 10_000;
    }

    /** Frais de retour d'un colis refusé : un pourcentage du prix de la course (hors commission). */
    public long returnFee(long courseFee) {
        return (courseFee * returnFeePercent + 50) / 100;
    }

    public int maxDistanceMeters() {
        return tiers.getLast().maxDistanceMeters();
    }
}
