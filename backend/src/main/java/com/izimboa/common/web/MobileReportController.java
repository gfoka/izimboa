package com.izimboa.common.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rapports techniques des applications mobiles (démarrage, plantage) : modèle du téléphone, version d'Android,
 * services Google, localisation, trace d'erreur. Sans connexion (un plantage peut survenir avant), sans donnée
 * personnelle, taille bornée. Inscrits au journal pour le diagnostic (workflow « Diagnostic Delivery »).
 */
@RestController
class MobileReportController {

    private static final Logger log = LoggerFactory.getLogger(MobileReportController.class);

    record Report(
            @NotBlank @Pattern(regexp = "START|CRASH") String kind,
            @Size(max = 40) String app,
            @Size(max = 40) String version,
            @Size(max = 100) String device,
            Integer android,
            Boolean googleServices,
            @Size(max = 100) String providers,
            Boolean locationEnabled,
            @Size(max = 16_000) String stack) {}

    @PostMapping("/v1/mobile/reports")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void report(@Valid @RequestBody Report r) {
        String head = "Mobile %s app=%s v=%s appareil=\"%s\" android=%s google=%s localisation=%s fournisseurs=[%s]".formatted(
                r.kind(), clean(r.app()), clean(r.version()), clean(r.device()), r.android(), r.googleServices(),
                r.locationEnabled(), clean(r.providers()));
        if (r.stack() == null) log.info(head);
        else log.warn("{}\n{}", head, r.stack());
    }

    /** Pas de retour à la ligne injecté dans le journal. */
    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("[\\r\\n]", " ");
    }
}
