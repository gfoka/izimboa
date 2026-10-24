package com.izimboa.delivery.pricing;

import com.izimboa.common.CurrentUser;
import com.izimboa.delivery.DeliveryProblem;
import com.izimboa.delivery.geo.GeoPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tarifs publics, simulation de prix (utilisable par la vitrine sans connexion)
 * et modification de la grille par l'administration.
 */
@RestController
@RequestMapping("/v1/delivery")
class PricingController {

    static final String CURRENCY = "XAF";

    private final PricingService pricing;

    PricingController(PricingService pricing) {
        this.pricing = pricing;
    }

    @GetMapping("/tariffs")
    TariffView tariffs() {
        return TariffView.of(pricing.currentTariff(), pricing.updatedAt());
    }

    @PutMapping("/tariffs")
    @PreAuthorize("hasRole('admin')")
    TariffView replace(@Valid @RequestBody TariffUpdate body, CurrentUser user) {
        Tariff tariff;
        try {
            tariff = body.toTariff();
        } catch (IllegalArgumentException e) {
            throw DeliveryProblem.invalid(e.getMessage());
        }
        pricing.replace(tariff, user.id());
        return TariffView.of(pricing.currentTariff(), pricing.updatedAt());
    }

    @PostMapping("/quotes")
    QuoteView quote(@Valid @RequestBody QuoteRequest body) {
        var q = pricing.quote(body.pickup(), body.dropoff(), Boolean.TRUE.equals(body.express()),
                body.codAmount() == null ? 0 : body.codAmount());
        return QuoteView.of(q);
    }

    // ---- Formats d'échange ----

    record TierView(BigDecimal maxDistanceKm, long price) {}

    record TariffView(String currency, List<TierView> tiers, long expressSurcharge,
                      BigDecimal codCommissionPercent, int returnFeePercent, Instant updatedAt) {
        static TariffView of(Tariff t, Instant updatedAt) {
            return new TariffView(CURRENCY,
                    t.tiers().stream().map(x -> new TierView(km(x.maxDistanceMeters()), x.price())).toList(),
                    t.expressSurcharge(),
                    BigDecimal.valueOf(t.codCommissionBp(), 2).stripTrailingZeros(),
                    t.returnFeePercent(), updatedAt);
        }
    }

    record TierUpdate(@NotNull @Positive BigDecimal maxDistanceKm, @NotNull @Positive Long price) {}

    record TariffUpdate(
            @NotEmpty @Size(max = 20) List<@Valid @NotNull TierUpdate> tiers,
            @NotNull @PositiveOrZero Long expressSurcharge,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal codCommissionPercent,
            @NotNull @Min(0) Integer returnFeePercent) {

        Tariff toTariff() {
            return new Tariff(
                    tiers.stream().map(t -> new Tariff.Tier(
                            t.maxDistanceKm().movePointRight(3).setScale(0, RoundingMode.HALF_UP).intValueExact(),
                            t.price())).toList(),
                    expressSurcharge,
                    codCommissionPercent.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact(),
                    returnFeePercent);
        }
    }

    record QuoteRequest(@NotNull @Valid GeoPoint pickup, @NotNull @Valid GeoPoint dropoff,
                        Boolean express, @PositiveOrZero Long codAmount) {}

    record QuoteView(String currency, BigDecimal distanceKm, boolean onRequest, long basePrice,
                     long expressSurcharge, long codCommission, long total) {
        static QuoteView of(Quote q) {
            return new QuoteView(CURRENCY, km(q.distanceMeters()), q.onRequest(), q.basePrice(),
                    q.expressSurcharge(), q.codCommission(), q.total());
        }
    }

    static BigDecimal km(int meters) {
        return BigDecimal.valueOf(meters).movePointLeft(3).setScale(1, RoundingMode.HALF_UP);
    }
}
