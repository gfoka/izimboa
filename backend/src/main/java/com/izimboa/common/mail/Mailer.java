package com.izimboa.common.mail;

/** Envoi d'e-mails transactionnels, derrière une interface remplaçable (décision n° 21 : Brevo au départ). */
public interface Mailer {

    record Message(String to, String toName, String replyTo, String subject, String text) {}

    /** Envoie le message. Ne lève pas d'exception : un échec est inscrit au journal (le message reste en base). */
    void send(Message message);
}
