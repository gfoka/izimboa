package com.izimboa.delivery.core;

/** Comment le vendeur paie la course. */
public enum FeePayment {
    /** Retenue sur l'argent encaissé auprès du destinataire : le vendeur n'avance rien. */
    DEDUCTED_FROM_COD,
    /** Payée à la création (Mobile Money), quand il n'y a rien à encaisser ou pas assez. */
    PREPAID
}
