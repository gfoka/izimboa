package com.izimboa.delivery.pricing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tariff_tier", schema = "delivery")
class TariffTierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "max_distance_m", nullable = false)
    private int maxDistanceMeters;

    @Column(name = "price_xaf", nullable = false)
    private long price;

    protected TariffTierEntity() {}

    TariffTierEntity(int maxDistanceMeters, long price) {
        this.maxDistanceMeters = maxDistanceMeters;
        this.price = price;
    }

    Tariff.Tier toTier() {
        return new Tariff.Tier(maxDistanceMeters, price);
    }
}
