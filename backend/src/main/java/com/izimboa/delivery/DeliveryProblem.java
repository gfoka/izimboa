package com.izimboa.delivery;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Erreur métier renvoyée au format standard « Problem Details » (RFC 9457) :
 * un code stable (« type ») que les applications peuvent traduire, et un message en français.
 */
public class DeliveryProblem extends ErrorResponseException {

    private DeliveryProblem(HttpStatus status, String code, String detail) {
        super(status, problem(status, code, detail), null);
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setType(URI.create("https://api.izimboa.com/problems/" + code));
        p.setProperty("code", code);
        return p;
    }

    public static DeliveryProblem notFound() {
        return new DeliveryProblem(HttpStatus.NOT_FOUND, "delivery-not-found", "Livraison introuvable.");
    }

    public static DeliveryProblem outsideServiceArea(String area) {
        return new DeliveryProblem(HttpStatus.UNPROCESSABLE_CONTENT, "outside-service-area",
                "Adresse hors de la zone desservie (" + area + ").");
    }

    public static DeliveryProblem quoteRequired() {
        return new DeliveryProblem(HttpStatus.UNPROCESSABLE_CONTENT, "quote-required",
                "Distance au-delà de la grille : prix sur devis, contactez-nous.");
    }

    public static DeliveryProblem invalidTransition(String from, String action) {
        return new DeliveryProblem(HttpStatus.CONFLICT, "invalid-transition",
                "Action « " + action + " » impossible : la livraison est à l'étape " + from + ".");
    }

    public static DeliveryProblem wrongHandoverCode(int remaining) {
        return new DeliveryProblem(HttpStatus.UNPROCESSABLE_CONTENT, "wrong-handover-code",
                "Code de remise incorrect. Essais restants : " + remaining + ".");
    }

    public static DeliveryProblem handoverLocked() {
        return new DeliveryProblem(HttpStatus.CONFLICT, "handover-locked",
                "Trop de codes incorrects : un opérateur doit débloquer la livraison.");
    }

    public static DeliveryProblem invalid(String detail) {
        return new DeliveryProblem(HttpStatus.UNPROCESSABLE_CONTENT, "invalid-request", detail);
    }
}
