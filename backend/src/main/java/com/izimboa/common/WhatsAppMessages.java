package com.izimboa.common;

import java.util.List;

/**
 * Envoi de messages WhatsApp à partir de modèles approuvés par Meta (numéro Izimboa, décision 73).
 * Ne lève jamais d'exception : le résultat dit si Meta a accepté le message.
 */
public interface WhatsAppMessages {

    /**
     * @param to        numéro au format +237…
     * @param name      nom du modèle chez Meta
     * @param language  langue du modèle (fr, en)
     * @param body      valeurs des variables {{1}}, {{2}}… du texte
     * @param urlSuffix fin de l'adresse du bouton lien du modèle (null s'il n'y en a pas)
     */
    record Template(String to, String name, String language, List<String> body, String urlSuffix) {}

    enum Status { SENT, FAILED, DISABLED }

    /** {@code messageId} : identifiant Meta (wamid) si accepté ; {@code error} : raison d'un échec. */
    record Result(Status status, String messageId, String error) {
        public static Result disabled() { return new Result(Status.DISABLED, null, null); }
    }

    Result send(Template template);
}
