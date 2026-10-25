package com.izimboa.delivery.courier;

import com.izimboa.common.CurrentUser;
import com.izimboa.delivery.DeliveryProblem;
import com.izimboa.delivery.core.DeliveryAssignments;
import com.izimboa.delivery.core.DeliveryStatus;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Position du livreur en direct, sur la carte du vendeur et du destinataire.
 * Le vendeur la voit dès l'attribution ; le destinataire seulement quand le colis est en route vers lui.
 * 204 quand il n'y a rien à montrer (pas de livreur, hors ligne, position trop ancienne).
 */
@RestController
class LiveTrackingController {

    record LivePosition(double lat, double lng, Float accuracyMeters, Instant at) {}

    private final DeliveryAssignments deliveries;
    private final CourierService couriers;

    LiveTrackingController(DeliveryAssignments deliveries, CourierService couriers) {
        this.deliveries = deliveries;
        this.couriers = couriers;
    }

    @GetMapping("/v1/delivery/deliveries/{id}/courier-position")
    ResponseEntity<LivePosition> forSender(@PathVariable UUID id, CurrentUser user) {
        if (!deliveries.visibleTo(id, user.id(), user.isStaff())) throw DeliveryProblem.notFound();
        return live(id, false);
    }

    @GetMapping("/v1/delivery/track/{token}/courier-position")
    ResponseEntity<LivePosition> forRecipient(@PathVariable @Size(max = 32) String token) {
        var id = deliveries.byTrackingToken(token).orElseThrow(DeliveryProblem::notFound);
        return live(id, true);
    }

    /** Chemin parcouru depuis le ramassage (jamais avant : la vie du livreur ne regarde personne). */
    @GetMapping("/v1/delivery/deliveries/{id}/trace")
    List<CourierService.TracePoint> traceForSender(@PathVariable UUID id, CurrentUser user) {
        if (!deliveries.visibleTo(id, user.id(), user.isStaff())) throw DeliveryProblem.notFound();
        return trace(id, false);
    }

    /** Le destinataire voit le chemin seulement pendant que le colis est en route vers lui. */
    @GetMapping("/v1/delivery/track/{token}/trace")
    List<CourierService.TracePoint> traceForRecipient(@PathVariable @Size(max = 32) String token) {
        var id = deliveries.byTrackingToken(token).orElseThrow(DeliveryProblem::notFound);
        return trace(id, true);
    }

    private List<CourierService.TracePoint> trace(UUID deliveryId, boolean recipient) {
        return deliveries.traceWindow(deliveryId)
                .filter(w -> !recipient || w.status() == DeliveryStatus.IN_TRANSIT)
                .map(w -> couriers.trace(w.courierId(), w.from(), w.to()))
                .orElse(List.of());
    }

    private ResponseEntity<LivePosition> live(UUID deliveryId, boolean recipient) {
        return deliveries.trackable(deliveryId)
                .filter(a -> !recipient || a.status() == DeliveryStatus.IN_TRANSIT)
                .flatMap(a -> couriers.livePosition(a.courierId()))
                .map(p -> ResponseEntity.ok(new LivePosition(p.lat(), p.lng(), p.accuracyMeters(), p.at())))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
