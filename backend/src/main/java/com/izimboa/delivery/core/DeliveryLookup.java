package com.izimboa.delivery.core;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lecture des livraisons pour les autres parties du module (messages des clients, notes des livreurs). */
@Service
public class DeliveryLookup {

    /** L'essentiel d'une livraison, pour retrouver de quoi parle un client. */
    public record DeliveryRef(UUID id, String reference, String status, String recipientName, String recipientPhone,
                              String packageDescription, UUID courierId, Instant createdAt) {}

    public record RatingStats(long count, double average) {}

    private final DeliveryRepository deliveries;
    private final DeliveryRatingRepository ratings;

    DeliveryLookup(DeliveryRepository deliveries, DeliveryRatingRepository ratings) {
        this.deliveries = deliveries;
        this.ratings = ratings;
    }

    @Transactional(readOnly = true)
    public Optional<DeliveryRef> byId(UUID id) {
        return deliveries.findById(id).map(DeliveryLookup::ref);
    }

    /** Livraisons récentes dont ce numéro est le destinataire, de la plus récente à la plus ancienne (10 au plus). */
    @Transactional(readOnly = true)
    public List<DeliveryRef> recentForRecipient(String phone, Instant since) {
        return deliveries.findAll((r, q, cb) -> cb.and(cb.equal(r.get("recipientPhone"), phone),
                        cb.greaterThanOrEqualTo(r.get("createdAt"), since)),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))).map(DeliveryLookup::ref).getContent();
    }

    @Transactional(readOnly = true)
    public Map<UUID, RatingStats> ratingsByCourier(Collection<UUID> couriers) {
        var out = new HashMap<UUID, RatingStats>();
        if (couriers.isEmpty()) return out;
        for (Object[] row : ratings.statsByCourier(couriers)) {
            out.put((UUID) row[0], new RatingStats(((Number) row[1]).longValue(), ((Number) row[2]).doubleValue()));
        }
        return out;
    }

    private static DeliveryRef ref(Delivery d) {
        return new DeliveryRef(d.getId(), d.getReference(), d.getStatus().name(), d.getRecipientName(), d.getRecipientPhone(),
                d.getPackageDescription(), d.getCourierId(), d.getCreatedAt());
    }
}
