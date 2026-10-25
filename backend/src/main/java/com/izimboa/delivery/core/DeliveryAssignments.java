package com.izimboa.delivery.core;

import com.izimboa.delivery.DeliveryProblem;
import java.time.Clock;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ce que l'attribution automatique et le suivi GPS ont besoin de savoir des livraisons
 * (les autres paquets du module ne touchent pas directement à l'entité).
 */
@Service
public class DeliveryAssignments {

    /** Étapes où le livreur a un colis en charge : il n'est pas libre pour une autre course. */
    public static final Set<DeliveryStatus> COURIER_BUSY =
            EnumSet.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT, DeliveryStatus.FAILED);

    /** Étapes où l'on montre la position du livreur au vendeur et au destinataire. */
    public static final Set<DeliveryStatus> TRACKABLE =
            EnumSet.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT);

    public record Waiting(UUID id, double pickupLat, double pickupLng, boolean express) {}

    public record Active(UUID deliveryId, UUID courierId, DeliveryStatus status) {}

    private final DeliveryRepository repository;
    private final Clock clock;

    DeliveryAssignments(DeliveryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Livraisons qui attendent un livreur : express d'abord, puis les plus anciennes. */
    @Transactional(readOnly = true)
    public List<Waiting> waitingForCourier() {
        Sort order = Sort.by(Sort.Order.desc("express"), Sort.Order.asc("createdAt"));
        return repository.findByStatus(DeliveryStatus.CREATED, order).stream()
                .map(d -> new Waiting(d.getId(), d.getPickupLat(), d.getPickupLng(), d.isExpress()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<UUID> busyCouriers() {
        return Set.copyOf(repository.findBusyCourierIds(COURIER_BUSY));
    }

    @Transactional(readOnly = true)
    public Optional<Active> activeFor(UUID courierId) {
        return repository.findFirstByCourierIdAndStatusIn(courierId, COURIER_BUSY)
                .map(d -> new Active(d.getId(), courierId, d.getStatus()));
    }

    @Transactional(readOnly = true)
    public Optional<DeliveryStatus> status(UUID deliveryId) {
        return repository.findById(deliveryId).map(Delivery::getStatus);
    }

    /** Résumé montré au livreur avant qu'il accepte : où ramasser, où livrer, ce qu'il gagne. */
    public record OfferDetails(UUID id, String reference, boolean express, double pickupLat, double pickupLng,
                               String pickupAddress, String pickupLandmark, String dropoffAddress,
                               String dropoffLandmark, int distanceMeters, long courseFee, long codAmount) {}

    @Transactional(readOnly = true)
    public Optional<OfferDetails> details(UUID deliveryId) {
        return repository.findById(deliveryId).map(d -> new OfferDetails(d.getId(), d.getReference(), d.isExpress(),
                d.getPickupLat(), d.getPickupLng(), d.getPickupAddress(), d.getPickupLandmark(),
                d.getDropoffAddress(), d.getDropoffLandmark(), d.getDistanceMeters(), d.courseFee(), d.getCodAmount()));
    }

    /** Le livreur a accepté : la livraison lui est attribuée (refusé si elle n'attend plus de livreur). */
    @Transactional
    public void assignAccepted(UUID deliveryId, UUID courierId) {
        var d = repository.findById(deliveryId).orElseThrow(DeliveryProblem::notFound);
        if (d.getStatus() != DeliveryStatus.CREATED) {
            throw DeliveryProblem.invalidTransition(d.getStatus().name(), "accept");
        }
        d.assign(courierId, courierId, clock.instant());
    }

    /** La livraison est-elle visible de cette personne (expéditeur, livreur attribué, opérateur) ? */
    @Transactional(readOnly = true)
    public boolean visibleTo(UUID deliveryId, UUID userId, boolean staff) {
        return repository.findById(deliveryId).filter(d -> d.isVisibleTo(userId, staff)).isPresent();
    }

    @Transactional(readOnly = true)
    public Optional<UUID> byTrackingToken(String token) {
        return repository.findByTrackingToken(token).map(Delivery::getId);
    }

    /** Période du tracé à montrer : du ramassage à la fin de la course (ou maintenant). */
    public record TraceWindow(UUID courierId, DeliveryStatus status, java.time.Instant from, java.time.Instant to) {}

    @Transactional(readOnly = true)
    public Optional<TraceWindow> traceWindow(UUID deliveryId) {
        return repository.findById(deliveryId)
                .filter(d -> d.getCourierId() != null && d.pickedUpAt() != null)
                .map(d -> new TraceWindow(d.getCourierId(), d.getStatus(), d.pickedUpAt(),
                        d.finishedAt() != null ? d.finishedAt() : clock.instant()));
    }

    /** Qui suit quelle livraison : le livreur attribué, si l'étape s'y prête. */
    @Transactional(readOnly = true)
    public Optional<Active> trackable(UUID deliveryId) {
        return repository.findById(deliveryId)
                .filter(d -> d.getCourierId() != null && TRACKABLE.contains(d.getStatus()))
                .map(d -> new Active(d.getId(), d.getCourierId(), d.getStatus()));
    }
}
