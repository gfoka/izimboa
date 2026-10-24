package com.izimboa.delivery.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Formats renvoyés par l'API. Le code de remise n'est jamais montré au livreur. */
final class DeliveryViews {

    private DeliveryViews() {}

    record StopView(double lat, double lng, String address, String landmark) {}

    record PricingView(String currency, long basePrice, long expressSurcharge, long codAmount, long codCommission,
                       long totalFee, FeePayment feePayment, Long returnFee, long sellerPayout) {}

    record EventView(DeliveryStatus status, Instant at, String note) {}

    record DeliveryView(UUID id, String reference, DeliveryStatus status, String trackingUrl,
                        StopView pickup, String pickupPhone, StopView dropoff, String recipientName,
                        String recipientPhone, String packageDescription, boolean express, BigDecimal distanceKm,
                        PricingView pricing, String handoverCode, int handoverFailures, UUID courierId,
                        int deliveryAttempts, Instant createdAt, Instant updatedAt, List<EventView> timeline) {}

    record DeliverySummary(UUID id, String reference, DeliveryStatus status, String dropoffAddress,
                           String recipientName, boolean express, long codAmount, long totalFee,
                           UUID courierId, Instant createdAt, Instant updatedAt) {}

    /** Suivi public (lien WhatsApp du destinataire) : aucune donnée personnelle. */
    record TrackingView(String reference, DeliveryStatus status, boolean express, String dropoffLandmark,
                        long amountToPay, String currency, List<EventView> timeline) {}

    static DeliveryView full(Delivery d, String trackingBaseUrl, boolean showHandoverCode) {
        return new DeliveryView(d.getId(), d.getReference(), d.getStatus(), trackingBaseUrl + d.getTrackingToken(),
                new StopView(d.getPickupLat(), d.getPickupLng(), d.getPickupAddress(), d.getPickupLandmark()),
                d.getPickupPhone(),
                new StopView(d.getDropoffLat(), d.getDropoffLng(), d.getDropoffAddress(), d.getDropoffLandmark()),
                d.getRecipientName(), d.getRecipientPhone(), d.getPackageDescription(), d.isExpress(),
                km(d.getDistanceMeters()),
                new PricingView("XAF", d.getBasePrice(), d.getExpressSurcharge(), d.getCodAmount(), d.getCodCommission(),
                        d.getTotalFee(), d.getFeePayment(), d.getReturnFee(), d.sellerPayout()),
                showHandoverCode ? d.getHandoverCode() : null, d.getHandoverFailures(), d.getCourierId(),
                d.getDeliveryAttempts(), d.getCreatedAt(), d.getUpdatedAt(), timeline(d));
    }

    static DeliverySummary summary(Delivery d) {
        return new DeliverySummary(d.getId(), d.getReference(), d.getStatus(), d.getDropoffAddress(),
                d.getRecipientName(), d.isExpress(), d.getCodAmount(), d.getTotalFee(), d.getCourierId(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    static TrackingView tracking(Delivery d) {
        return new TrackingView(d.getReference(), d.getStatus(), d.isExpress(), d.getDropoffLandmark(),
                d.getCodAmount(), "XAF", timeline(d));
    }

    private static List<EventView> timeline(Delivery d) {
        return d.getEvents().stream().map(e -> new EventView(e.status(), e.occurredAt(), publicNote(e))).toList();
    }

    /** Les notes internes d'attribution (identifiant du livreur) ne sortent pas. */
    private static String publicNote(DeliveryEvent e) {
        return e.status() == DeliveryStatus.ASSIGNED ? null : e.note();
    }

    private static BigDecimal km(int meters) {
        return BigDecimal.valueOf(meters).movePointLeft(3).setScale(1, RoundingMode.HALF_UP);
    }
}
