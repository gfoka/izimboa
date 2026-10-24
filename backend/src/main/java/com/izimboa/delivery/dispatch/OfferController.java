package com.izimboa.delivery.dispatch;

import com.izimboa.common.CurrentUser;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    /** 200 avec la proposition, ou 204 s'il n'y en a pas (l'application interroge toutes les quelques secondes). */
    @GetMapping("/current")
    ResponseEntity<DispatchService.OfferView> current(CurrentUser user) {
        return dispatch.currentOffer(user.id()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

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
