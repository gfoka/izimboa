package com.izimboa.delivery.pricing;

import com.izimboa.delivery.DeliveryProblem;
import com.izimboa.delivery.DeliveryProperties;
import com.izimboa.delivery.geo.GeoPoint;
import com.izimboa.delivery.geo.RouteDistance;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calcule le prix d'une course à partir de la grille en base et de la distance par la route. */
@Service
public class PricingService {

    private final TariffTierRepository tiers;
    private final PricingSettingsRepository settings;
    private final RouteDistance routeDistance;
    private final DeliveryProperties.Area serviceArea;
    private final Clock clock;

    PricingService(TariffTierRepository tiers, PricingSettingsRepository settings, RouteDistance routeDistance,
                   DeliveryProperties properties, Clock clock) {
        this.tiers = tiers;
        this.settings = settings;
        this.routeDistance = routeDistance;
        this.serviceArea = properties.serviceArea();
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Tariff currentTariff() {
        var s = loadSettings();
        return new Tariff(tiers.findAll().stream().map(TariffTierEntity::toTier).toList(),
                s.expressSurcharge(), s.codCommissionBp(), s.returnFeePercent());
    }

    @Transactional(readOnly = true)
    public Instant updatedAt() {
        return loadSettings().updatedAt();
    }

    /** Prix d'une course entre deux points. Refuse les adresses hors de la zone desservie. */
    @Transactional(readOnly = true)
    public Quote quote(GeoPoint pickup, GeoPoint dropoff, boolean express, long codAmount) {
        requireInServiceArea(pickup);
        requireInServiceArea(dropoff);
        return currentTariff().quote(routeDistance.meters(pickup, dropoff), express, codAmount);
    }

    /** Remplace toute la grille (administration). Les livraisons déjà créées gardent leur prix. */
    @Transactional
    public Tariff replace(Tariff tariff, UUID by) {
        tiers.deleteAllInBatch();
        tiers.flush();
        tiers.saveAll(tariff.tiers().stream().map(t -> new TariffTierEntity(t.maxDistanceMeters(), t.price())).toList());
        loadSettings().update(tariff, by, clock.instant());
        return tariff;
    }

    private void requireInServiceArea(GeoPoint p) {
        if (!serviceArea.contains(p.lat(), p.lng())) {
            throw DeliveryProblem.outsideServiceArea(serviceArea.name());
        }
    }

    private PricingSettingsEntity loadSettings() {
        return settings.findById(PricingSettingsEntity.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Réglages de prix absents (migration V2)"));
    }
}
