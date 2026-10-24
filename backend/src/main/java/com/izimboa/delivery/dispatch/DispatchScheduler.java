package com.izimboa.delivery.dispatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Tourne toutes les 3 secondes : une nouvelle commande est proposée presque immédiatement. */
@Component
@ConditionalOnProperty(name = "izimboa.delivery.dispatch.enabled", havingValue = "true", matchIfMissing = true)
class DispatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DispatchScheduler.class);

    private final DispatchService dispatch;

    DispatchScheduler(DispatchService dispatch) {
        this.dispatch = dispatch;
    }

    @Scheduled(fixedDelay = 3000, initialDelay = 10000)
    void tick() {
        try {
            dispatch.runOnce();
        } catch (RuntimeException e) {
            // Conflit ponctuel (deux réponses au même moment) : le tour suivant reprend proprement.
            log.warn("Tour d'attribution interrompu : {}", e.getMessage());
        }
    }
}
