package com.izimboa.delivery.geo;

import com.izimboa.common.CurrentUser;
import com.izimboa.delivery.DeliveryProblem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Aide à placer une adresse : lire une localisation collée, chercher un lieu. */
@RestController
class GeoController {

    private final LocationLinkResolver resolver;
    private final PlaceSearch places;

    GeoController(LocationLinkResolver resolver, PlaceSearch places) {
        this.resolver = resolver;
        this.places = places;
    }

    record ResolveRequest(@NotBlank @Size(max = 2000) String text) {}

    /** Coordonnées d'une localisation WhatsApp / Google Maps collée par le vendeur (connecté). */
    @PostMapping("/v1/delivery/locations/resolve")
    GeoPoint resolve(@Valid @RequestBody ResolveRequest body, CurrentUser user) {
        return resolver.resolve(body.text()).orElseThrow(() -> DeliveryProblem.invalid(
                "Impossible de lire une position dans ce lien. Collez le lien de localisation reçu du client "
                        + "(WhatsApp ou Google Maps), ou placez l'épingle sur la carte."));
    }

    /** Recherche de lieux au Cameroun, aussi pour le destinataire (lien de suivi, sans connexion). */
    @GetMapping("/v1/delivery/places")
    List<PlaceSearch.Place> search(@RequestParam @Size(max = 120) String q,
                                   @RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double lat,
                                   @RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double lng) {
        return places.search(q, lat != null && lng != null ? new GeoPoint(lat, lng) : null);
    }
}
