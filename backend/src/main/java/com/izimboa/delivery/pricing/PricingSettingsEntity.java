package com.izimboa.delivery.pricing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Réglages de prix (une seule ligne, id = 1). */
@Entity
@Table(name = "pricing_settings", schema = "delivery")
class PricingSettingsEntity {

    static final short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column(name = "express_surcharge_xaf", nullable = false)
    private long expressSurcharge;

    @Column(name = "cod_commission_bp", nullable = false)
    private int codCommissionBp;

    @Column(name = "return_fee_percent", nullable = false)
    private int returnFeePercent;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    protected PricingSettingsEntity() {}

    void update(Tariff tariff, UUID by, Instant at) {
        this.expressSurcharge = tariff.expressSurcharge();
        this.codCommissionBp = tariff.codCommissionBp();
        this.returnFeePercent = tariff.returnFeePercent();
        this.updatedBy = by;
        this.updatedAt = at;
    }

    long expressSurcharge() { return expressSurcharge; }
    int codCommissionBp() { return codCommissionBp; }
    int returnFeePercent() { return returnFeePercent; }
    Instant updatedAt() { return updatedAt; }
}
