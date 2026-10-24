package com.izimboa.delivery.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

interface TariffTierRepository extends JpaRepository<TariffTierEntity, Integer> {}

interface PricingSettingsRepository extends JpaRepository<PricingSettingsEntity, Short> {}
