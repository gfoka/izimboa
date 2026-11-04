package com.izimboa.delivery.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.izimboa.delivery.dispatch.OfferMade;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Nouvelle course proposée : un message Firebase réveille l'application du livreur, qui fait sonner l'offre
 * (décision 76). C'est un second canal : quand l'application tourne, elle reçoit déjà l'offre par son service
 * « en ligne » ; Firebase couvre le cas où Android l'a endormie ou arrêtée.
 */
@Service
class CourierPush {

    private static final Logger log = LoggerFactory.getLogger(CourierPush.class);

    private final PushTokenRepository tokens;
    private final FcmClient fcm;
    private final Executor executor;

    @Autowired
    CourierPush(PushTokenRepository tokens, Clock clock, @Value("${izimboa.push.service-account:}") String serviceAccount) {
        this(tokens, FcmClient.from(serviceAccount, new ObjectMapper(), clock));
    }

    CourierPush(PushTokenRepository tokens, FcmClient fcm) {
        this.tokens = tokens;
        this.fcm = fcm;
        this.executor = new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(500),
                r -> { var t = new Thread(r, "push-livreur"); t.setDaemon(true); return t; },
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onOffer(OfferMade offer) {
        if (!fcm.enabled()) return;
        executor.execute(() -> send(offer));
    }

    void send(OfferMade offer) {
        var data = Map.of("type", "offer", "offerId", offer.offerId().toString());
        for (var t : tokens.findByUserId(offer.courierId())) {
            var outcome = fcm.send(t.token(), data, offer.secondsLeft());
            if (outcome == FcmClient.Outcome.UNREGISTERED) {
                tokens.deleteById(t.token());
                log.info("Jeton push périmé retiré (livreur {}).", offer.courierId());
            } else {
                log.info("Push « nouvelle course » au livreur {} : {}", offer.courierId(), outcome);
            }
        }
    }
}
