package com.izimboa.delivery.core;

import com.izimboa.common.CurrentUser;
import com.izimboa.delivery.geo.GeoPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Livraisons : création par le vendeur, étapes par le livreur, supervision par l'opérateur. */
@RestController
@RequestMapping("/v1/delivery")
class DeliveryController {

    private final DeliveryService service;

    DeliveryController(DeliveryService service) {
        this.service = service;
    }

    @PostMapping("/deliveries")
    @PreAuthorize("hasAnyRole('vendeur', 'client')")
    ResponseEntity<DeliveryViews.DeliveryView> create(
            @Valid @RequestBody CreateRequest body,
            @RequestHeader(name = "Idempotency-Key", required = false) @Size(max = 80) String idempotencyKey,
            CurrentUser user) {
        var created = service.create(body.toCommand(), blankToNull(idempotencyKey), user);
        var view = created.view();
        if (created.replayed()) return ResponseEntity.ok(view);
        return ResponseEntity.created(URI.create("/v1/delivery/deliveries/" + view.id())).body(view);
    }

    @GetMapping("/deliveries")
    List<DeliveryViews.DeliverySummary> list(@RequestParam(required = false) DeliveryService.Scope scope,
                                             @RequestParam(required = false) DeliveryStatus status,
                                             CurrentUser user) {
        return service.list(user, scope, status);
    }

    @GetMapping("/deliveries/{id}")
    DeliveryViews.DeliveryView get(@PathVariable UUID id, CurrentUser user) {
        return service.get(id, user);
    }

    /** Suivi public pour le destinataire (lien WhatsApp), sans connexion. */
    @GetMapping("/track/{token}")
    DeliveryViews.TrackingView track(@PathVariable @Size(max = 32) String token) {
        return service.track(token);
    }

    @PostMapping("/deliveries/{id}/cancel")
    DeliveryViews.DeliveryView cancel(@PathVariable UUID id, @Valid @RequestBody(required = false) ReasonRequest body,
                                      CurrentUser user) {
        return service.cancel(id, body == null ? null : body.reason(), user);
    }

    @PostMapping("/deliveries/{id}/assign")
    @PreAuthorize("hasAnyRole('operateur', 'admin')")
    DeliveryViews.DeliveryView assign(@PathVariable UUID id, @Valid @RequestBody AssignRequest body, CurrentUser user) {
        return service.assign(id, body.courierId(), user);
    }

    @PostMapping("/deliveries/{id}/pickup")
    @PreAuthorize("hasAnyRole('livreur', 'operateur', 'admin')")
    DeliveryViews.DeliveryView pickUp(@PathVariable UUID id, @Valid @RequestBody(required = false) PositionRequest body,
                                      CurrentUser user) {
        return service.pickUp(id, position(body), user);
    }

    @PostMapping("/deliveries/{id}/depart")
    @PreAuthorize("hasAnyRole('livreur', 'operateur', 'admin')")
    DeliveryViews.DeliveryView depart(@PathVariable UUID id, @Valid @RequestBody(required = false) PositionRequest body,
                                      CurrentUser user) {
        return service.depart(id, position(body), user);
    }

    @PostMapping("/deliveries/{id}/deliver")
    @PreAuthorize("hasAnyRole('livreur', 'operateur', 'admin')")
    DeliveryViews.DeliveryView deliver(@PathVariable UUID id, @Valid @RequestBody DeliverRequest body, CurrentUser user) {
        return service.deliver(id, body.code(), new Delivery.Position(body.lat(), body.lng(), body.occurredAt()), user);
    }

    @PostMapping("/deliveries/{id}/fail")
    @PreAuthorize("hasAnyRole('livreur', 'operateur', 'admin')")
    DeliveryViews.DeliveryView fail(@PathVariable UUID id, @Valid @RequestBody FailRequest body, CurrentUser user) {
        return service.fail(id, body.reason(), new Delivery.Position(body.lat(), body.lng(), body.occurredAt()), user);
    }

    @PostMapping("/deliveries/{id}/retry")
    @PreAuthorize("hasAnyRole('livreur', 'operateur', 'admin')")
    DeliveryViews.DeliveryView retry(@PathVariable UUID id, @Valid @RequestBody(required = false) PositionRequest body,
                                     CurrentUser user) {
        return service.retry(id, position(body), user);
    }

    @PostMapping("/deliveries/{id}/return")
    @PreAuthorize("hasAnyRole('livreur', 'operateur', 'admin')")
    DeliveryViews.DeliveryView returnToSender(@PathVariable UUID id,
                                              @Valid @RequestBody(required = false) PositionRequest body,
                                              CurrentUser user) {
        return service.returnToSender(id, position(body), user);
    }

    @PostMapping("/deliveries/{id}/unlock-handover")
    @PreAuthorize("hasAnyRole('operateur', 'admin')")
    DeliveryViews.DeliveryView unlockHandover(@PathVariable UUID id, CurrentUser user) {
        return service.unlockHandover(id, user);
    }

    // ---- Formats d'entrée ----

    record CreateRequest(
            @NotNull @Valid GeoPoint pickup,
            @NotBlank @Size(max = 300) String pickupAddress,
            @Size(max = 300) String pickupLandmark,
            @NotBlank @Size(max = 25) String pickupPhone,
            @NotNull @Valid GeoPoint dropoff,
            @NotBlank @Size(max = 300) String dropoffAddress,
            @Size(max = 300) String dropoffLandmark,
            @NotBlank @Size(max = 120) String recipientName,
            @NotBlank @Size(max = 25) String recipientPhone,
            @Size(max = 200) String packageDescription,
            Boolean express,
            @PositiveOrZero Long codAmount) {

        DeliveryService.NewDelivery toCommand() {
            return new DeliveryService.NewDelivery(pickup, pickupAddress, pickupLandmark, pickupPhone,
                    dropoff, dropoffAddress, dropoffLandmark, recipientName, recipientPhone, packageDescription,
                    Boolean.TRUE.equals(express), codAmount == null ? 0 : codAmount);
        }
    }

    record AssignRequest(@NotNull UUID courierId) {}

    record ReasonRequest(@Size(max = 300) String reason) {}

    /** {@code occurredAt} : heure du téléphone, quand l'action a été faite sans réseau et envoyée plus tard. */
    record PositionRequest(@DecimalMin("-90") @DecimalMax("90") Double lat,
                           @DecimalMin("-180") @DecimalMax("180") Double lng,
                           Instant occurredAt) {}

    record DeliverRequest(@NotBlank @Pattern(regexp = "\\d{4}", message = "4 chiffres") String code,
                          @DecimalMin("-90") @DecimalMax("90") Double lat,
                          @DecimalMin("-180") @DecimalMax("180") Double lng,
                          Instant occurredAt) {}

    record FailRequest(@NotBlank @Size(max = 300) String reason,
                       @DecimalMin("-90") @DecimalMax("90") Double lat,
                       @DecimalMin("-180") @DecimalMax("180") Double lng,
                       Instant occurredAt) {}

    private static Delivery.Position position(PositionRequest p) {
        return p == null ? null : new Delivery.Position(p.lat(), p.lng(), p.occurredAt());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }
}
