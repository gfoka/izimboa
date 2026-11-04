package com.izimboa.delivery.notify;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipientNotificationTest {

    @Test
    void lesAccusesArrivesDansLeDesordreNeReculentPas() {
        var n = new RecipientNotification(UUID.randomUUID(), RecipientNotice.Kind.CREATED, "x", Instant.now());
        n.whatsAppStatus("read", null, Instant.now());
        n.whatsAppStatus("delivered", null, Instant.now());
        assertThat(n.getDeliveryStatus()).isEqualTo("read");
        n.whatsAppStatus("failed", "131026 Message undeliverable", Instant.now());
        assertThat(n.getDeliveryStatus()).isEqualTo("failed");
        assertThat(n.getDeliveryError()).contains("131026");
    }
}
