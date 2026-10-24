package com.izimboa.delivery.dispatch;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Attribution automatique (application.yml, préfixe izimboa.delivery.dispatch).
 *
 * @param enabled             tâche de fond active (désactivée dans les tests, qui l'appellent eux-mêmes)
 * @param offerTimeoutSeconds temps laissé au livreur pour accepter
 * @param maxRadiusKm         distance maximale (vol d'oiseau) entre le livreur et le ramassage
 * @param courierSharePercent part du prix de la course qui revient au livreur (affichée avant d'accepter)
 */
@ConfigurationProperties("izimboa.delivery.dispatch")
public record DispatchProperties(Boolean enabled, Integer offerTimeoutSeconds, Double maxRadiusKm, Integer courierSharePercent) {

    public DispatchProperties {
        if (enabled == null) enabled = true;
        if (offerTimeoutSeconds == null) offerTimeoutSeconds = 30;
        if (maxRadiusKm == null) maxRadiusKm = 8.0;
        if (courierSharePercent == null) courierSharePercent = 65;
    }
}
