package com.izimboa.delivery.push;

import com.izimboa.common.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Clock;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** L'application livreur enregistre le jeton Firebase de son téléphone (à la connexion et à chaque changement). */
@RestController
@RequestMapping("/v1/delivery/courier/push-token")
@PreAuthorize("hasRole('livreur')")
class PushController {

    private final PushTokenRepository tokens;
    private final Clock clock;

    PushController(PushTokenRepository tokens, Clock clock) {
        this.tokens = tokens;
        this.clock = clock;
    }

    record TokenRequest(@NotBlank @Size(max = 512) String token, @Size(max = 40) String appVersion) {}

    @PutMapping
    @Transactional
    ResponseEntity<Void> register(@Valid @RequestBody TokenRequest body, CurrentUser user) {
        var now = clock.instant();
        // Un même téléphone peut changer de compte : le jeton suit le dernier livreur connecté.
        tokens.findById(body.token()).ifPresentOrElse(
                t -> t.refresh(user.id(), body.appVersion(), now),
                () -> tokens.save(new PushToken(body.token(), user.id(), body.appVersion(), now)));
        return ResponseEntity.noContent().build();
    }

    /** Déconnexion : ce téléphone ne reçoit plus les courses de ce compte. */
    @DeleteMapping
    ResponseEntity<Void> unregister(@Valid @RequestBody TokenRequest body, CurrentUser user) {
        tokens.deleteByTokenAndUserId(body.token(), user.id());
        return ResponseEntity.noContent().build();
    }
}
