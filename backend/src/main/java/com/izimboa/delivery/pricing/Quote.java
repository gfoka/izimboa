package com.izimboa.delivery.pricing;

/**
 * Prix d'une course, en francs CFA.
 *
 * @param onRequest vrai si la distance dépasse la grille : prix sur devis, aucun montant
 */
public record Quote(int distanceMeters, boolean onRequest, long basePrice, long expressSurcharge,
                    long codCommission, long total) {

    static Quote onRequest(int distanceMeters) {
        return new Quote(distanceMeters, true, 0, 0, 0, 0);
    }

    /** Prix de la course seule (sans la commission sur l'encaissement). */
    public long courseFee() {
        return basePrice + expressSurcharge;
    }
}
