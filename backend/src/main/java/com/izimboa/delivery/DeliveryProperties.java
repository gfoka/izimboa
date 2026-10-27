package com.izimboa.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Réglages techniques de Delivery (application.yml, préfixe izimboa.delivery).
 * Les tarifs, eux, sont en base et modifiables par l'administration.
 *
 * @param roadFactor  distance à vol d'oiseau × ce facteur ≈ distance par la route
 *                    (en attendant le moteur d'itinéraires OpenStreetMap)
 * @param serviceAreas villes desservies (rectangles) ; ramassage et livraison doivent être dans la même ville
 * @param trackingBaseUrl début du lien de suivi envoyé au destinataire (suivi du jeton)
 */
@Validated
@ConfigurationProperties("izimboa.delivery")
public record DeliveryProperties(
        @NotNull @DecimalMin("1.0") Double roadFactor,
        @NotEmpty List<@Valid @NotNull Area> serviceAreas,
        @NotBlank String trackingBaseUrl) {

    /** Ville qui contient ce point, s'il y en a une. */
    public java.util.Optional<Area> areaOf(double lat, double lng) {
        return serviceAreas.stream().filter(a -> a.contains(lat, lng)).findFirst();
    }

    public record Area(String name, double south, double north, double west, double east) {
        public boolean contains(double lat, double lng) {
            return lat >= south && lat <= north && lng >= west && lng <= east;
        }
    }
}
