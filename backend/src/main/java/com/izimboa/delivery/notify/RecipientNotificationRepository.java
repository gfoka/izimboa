package com.izimboa.delivery.notify;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface RecipientNotificationRepository extends JpaRepository<RecipientNotification, Long> {

    boolean existsByDeliveryIdAndKind(UUID deliveryId, RecipientNotice.Kind kind);

    List<RecipientNotification> findByDeliveryIdOrderByCreatedAtAsc(UUID deliveryId);

    java.util.Optional<RecipientNotification> findFirstByMessageId(String messageId);
}
