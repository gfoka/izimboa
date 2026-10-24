package com.izimboa.delivery.core;

/**
 * Étapes d'une livraison.
 * <pre>
 * CREATED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
 *    │          │                     │   ↑
 *    └──────────┴→ CANCELLED          ↓   │ (nouvelle tentative)
 *                                   FAILED → RETURNED
 * </pre>
 */
public enum DeliveryStatus {
    CREATED, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED, RETURNED, CANCELLED;

    public boolean isFinal() {
        return this == DELIVERED || this == RETURNED || this == CANCELLED;
    }
}
