package com.izimboa.delivery.notify;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

interface InboundMessageRepository extends JpaRepository<InboundMessage, Long> {

    boolean existsByWaMessageId(String waMessageId);

    /** Une réponse automatique a déjà été envoyée à ce numéro récemment. */
    boolean existsByFromPhoneAndAutoRepliedTrueAndReceivedAtAfter(String fromPhone, Instant after);
}
