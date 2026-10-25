package com.izimboa.delivery.dispatch;

import com.izimboa.common.CurrentUser;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Application livreur : la course proposée, accepter, refuser. */
@RestController
@RequestMapping("/v1/delivery/courier/offers")
@PreAuthorize("hasRole('livreur')")
class OfferController {

    private final DispatchService dispatch;

    OfferController(DispatchService dispatch) {
        this.dispatch = dispatch;
    }

    /**
     * 200 avec la proposition, ou 204 s'il n'y en a pas.
     * {@code wait} (secondes, 25 max) : attente longue — la réponse part dès qu'une course arrive. Le téléphone
     * ne réveille sa radio que 2 à 3 fois par minute au lieu de 15 : grosse économie de batterie (décision n° 61).
     */
    @GetMapping("/current")
    ResponseEntity<DispatchService.OfferView> current(@RequestParam(defaultValue = "0") int wait, CurrentUser user)
            throws InterruptedException {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(Math.clamp(wait, 0, MAX_WAIT_SECONDS));
        while (true) {
            var offer = dispatch.currentOffer(user.id());
            if (offer.isPresent()) return ResponseEntity.ok(offer.get());
            if (System.nanoTime() >= deadline) return ResponseEntity.noContent().build();
            Thread.sleep(1000); // fil virtuel : l'attente ne bloque aucun fil du système
        }
    }

    static final int MAX_WAIT_SECONDS = 25;

    record Accepted(UUID deliveryId) {}

    @PostMapping("/{offerId}/accept")
    Accepted accept(@PathVariable UUID offerId, CurrentUser user) {
        return new Accepted(dispatch.accept(offerId, user.id()));
    }

    @PostMapping("/{offerId}/decline")
    ResponseEntity<Void> decline(@PathVariable UUID offerId, CurrentUser user) {
        dispatch.decline(offerId, user.id());
        return ResponseEntity.noContent().build();
    }
}
