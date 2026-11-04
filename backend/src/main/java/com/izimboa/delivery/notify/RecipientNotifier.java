package com.izimboa.delivery.notify;

import com.izimboa.common.WhatsAppMessages;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envoie au destinataire, sur WhatsApp, le lien de suivi à la création, « en route » au départ du livreur
 * et « livré » à la remise (décision 73). Après l'enregistrement de l'étape, hors de la requête :
 * un WhatsApp lent ne ralentit ni le vendeur ni le livreur. Un seul message par étape, même si elle se répète.
 */
@Service
public class RecipientNotifier {

    private static final Logger log = LoggerFactory.getLogger(RecipientNotifier.class);

    /** Ce que le vendeur voit de l'envoi automatique, étape par étape. */
    public record NoticeView(String kind, String status, Instant at, String whatsApp, String whatsAppError) {}

    private final RecipientNotificationRepository repository;
    private final WhatsAppMessages whatsApp;
    private final Clock clock;
    private final Executor executor;

    RecipientNotifier(RecipientNotificationRepository repository, WhatsAppMessages whatsApp, Clock clock) {
        this.repository = repository;
        this.whatsApp = whatsApp;
        this.clock = clock;
        // Deux envois en parallèle au plus ; file bornée, au-delà l'envoi se fait dans le fil de l'appelant.
        this.executor = new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(500),
                r -> { var t = new Thread(r, "whatsapp-destinataire"); t.setDaemon(true); return t; },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onNotice(RecipientNotice notice) {
        executor.execute(() -> send(notice));
    }

    /** Envoi immédiat (utilisé aussi par les tests). */
    void send(RecipientNotice notice) {
        try {
            if (repository.existsByDeliveryIdAndKind(notice.deliveryId(), notice.kind())) return;
            var template = RecipientMessages.of(notice);
            RecipientNotification row;
            try {
                row = repository.saveAndFlush(new RecipientNotification(notice.deliveryId(), notice.kind(), template.name(), clock.instant()));
            } catch (DataIntegrityViolationException alreadyThere) {
                return;
            }
            var result = whatsApp.send(template);
            // Nouveau modèle « livré » (bouton « Noter la livraison ») pas encore validé par Meta : ancien modèle.
            if (result.status() == WhatsAppMessages.Status.FAILED && notice.kind() == RecipientNotice.Kind.DELIVERED) {
                var fallback = RecipientMessages.deliveredFallback(notice);
                result = whatsApp.send(fallback);
            }
            row.finish(RecipientNotification.Status.valueOf(result.status().name()), result.messageId(), result.error(), clock.instant());
            repository.save(row);
        } catch (Exception e) {
            log.error("Notification WhatsApp {} de la livraison {} : {}", notice.kind(), notice.deliveryId(), e.getMessage());
        }
    }

    public List<NoticeView> notices(UUID deliveryId) {
        return repository.findByDeliveryIdOrderByCreatedAtAsc(deliveryId).stream()
                .map(n -> new NoticeView(n.getKind().name(), n.getStatus().name(), n.getUpdatedAt(),
                        n.getDeliveryStatus(), n.getDeliveryError())).toList();
    }
}
