package com.izimboa.delivery.dispatch;

import com.izimboa.delivery.DeliveryProblem;
import com.izimboa.delivery.core.DeliveryAssignments;
import com.izimboa.delivery.core.DeliveryStatus;
import com.izimboa.delivery.courier.CourierService;
import com.izimboa.delivery.geo.GeoPoint;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Attribution automatique : chaque livraison en attente est proposée au livreur libre le plus proche
 * du point de ramassage. Sans réponse dans le délai, ou en cas de refus, elle passe au suivant.
 * L'opérateur peut toujours attribuer à la main.
 */
@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);
    private static final Set<DeliveryOffer.Status> PASSED = EnumSet.of(DeliveryOffer.Status.DECLINED, DeliveryOffer.Status.EXPIRED);

    private final DeliveryOfferRepository offers;
    private final DeliveryAssignments deliveries;
    private final CourierService couriers;
    private final DispatchProperties properties;
    private final Clock clock;

    DispatchService(DeliveryOfferRepository offers, DeliveryAssignments deliveries, CourierService couriers,
                    DispatchProperties properties, Clock clock) {
        this.offers = offers;
        this.deliveries = deliveries;
        this.couriers = couriers;
        this.properties = properties;
        this.clock = clock;
    }

    /** Un tour : clôt les propositions périmées, puis propose chaque livraison en attente. Retourne le nombre de propositions faites. */
    @Transactional
    public int runOnce() {
        Instant now = clock.instant();

        // 1. Propositions en attente : expirées, ou devenues inutiles (livraison annulée ou attribuée à la main).
        for (var o : offers.findByStatus(DeliveryOffer.Status.OFFERED)) {
            if (!now.isBefore(o.expiresAt())) {
                o.close(DeliveryOffer.Status.EXPIRED, now);
            } else if (deliveries.status(o.deliveryId()).filter(s -> s == DeliveryStatus.CREATED).isEmpty()) {
                o.close(DeliveryOffer.Status.CANCELLED, now);
            }
        }
        offers.flush();

        // 2. Livreurs libres : en ligne, position récente, sans colis en charge, sans proposition en cours.
        Set<UUID> unavailable = new HashSet<>(deliveries.busyCouriers());
        Set<UUID> deliveriesWithOffer = new HashSet<>();
        for (var o : offers.findByStatus(DeliveryOffer.Status.OFFERED)) {
            unavailable.add(o.courierId());
            deliveriesWithOffer.add(o.deliveryId());
        }
        var free = couriers.onlineWithFreshPosition().stream().filter(c -> !unavailable.contains(c.courierId())).toList();
        if (free.isEmpty()) return 0;

        // 3. Express d'abord, puis les plus anciennes.
        int made = 0;
        Set<UUID> taken = new HashSet<>();
        double maxMeters = properties.maxRadiusKm() * 1000;
        for (var w : deliveries.waitingForCourier()) {
            if (deliveriesWithOffer.contains(w.id())) continue;
            var pickup = new GeoPoint(w.pickupLat(), w.pickupLng());
            Set<UUID> passed = Set.copyOf(offers.couriersWhoPassed(w.id(), PASSED));
            var best = free.stream()
                    .filter(c -> !taken.contains(c.courierId()) && !passed.contains(c.courierId()))
                    .map(c -> new Candidate(c.courierId(), pickup.straightLineMetersTo(
                            new GeoPoint(c.position().lat(), c.position().lng()))))
                    .filter(c -> c.meters() <= maxMeters)
                    .min(Comparator.comparingDouble(Candidate::meters));
            if (best.isEmpty()) continue;
            var c = best.get();
            offers.save(new DeliveryOffer(w.id(), c.courierId(), (int) Math.round(c.meters()), now,
                    now.plusSeconds(properties.offerTimeoutSeconds())));
            taken.add(c.courierId());
            made++;
            log.info("Course {} proposée au livreur {} ({} m)", w.id(), c.courierId(), Math.round(c.meters()));
        }
        return made;
    }

    private record Candidate(UUID courierId, double meters) {}

    // ---- Côté livreur ----

    /** Ce que voit le livreur : la course proposée, le temps restant et ce qu'il gagnera. */
    public record OfferView(UUID offerId, int secondsLeft, int distanceToPickupMeters, long courierEarning,
                            DeliveryAssignments.OfferDetails delivery) {}

    @Transactional(readOnly = true)
    public Optional<OfferView> currentOffer(UUID courierId) {
        Instant now = clock.instant();
        return offers.findFirstByCourierIdAndStatus(courierId, DeliveryOffer.Status.OFFERED)
                .filter(o -> o.pending(now))
                .flatMap(o -> deliveries.details(o.deliveryId()).map(d -> new OfferView(o.id(),
                        (int) Math.max(0, o.expiresAt().getEpochSecond() - now.getEpochSecond()),
                        o.distanceMeters(), earning(d.courseFee()), d)));
    }

    @Transactional
    public UUID accept(UUID offerId, UUID courierId) {
        Instant now = clock.instant();
        var o = mine(offerId, courierId);
        if (!o.pending(now)) throw DeliveryProblem.invalid("Cette proposition a expiré.");
        deliveries.assignAccepted(o.deliveryId(), courierId);
        o.close(DeliveryOffer.Status.ACCEPTED, now);
        return o.deliveryId();
    }

    @Transactional
    public void decline(UUID offerId, UUID courierId) {
        Instant now = clock.instant();
        var o = mine(offerId, courierId);
        if (o.status() == DeliveryOffer.Status.OFFERED) o.close(DeliveryOffer.Status.DECLINED, now);
    }

    private DeliveryOffer mine(UUID offerId, UUID courierId) {
        return offers.findById(offerId).filter(o -> o.courierId().equals(courierId))
                .orElseThrow(DeliveryProblem::notFound);
    }

    long earning(long courseFee) {
        return Math.round(courseFee * properties.courierSharePercent() / 100.0);
    }
}
