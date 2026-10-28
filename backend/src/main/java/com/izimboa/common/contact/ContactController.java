package com.izimboa.common.contact;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Formulaires publics du site izimboa.com (sans connexion), et liste des demandes pour l'équipe. */
@RestController
class ContactController {

    private final ContactService service;

    ContactController(ContactService service) {
        this.service = service;
    }

    record Body(
            @NotBlank @Pattern(regexp = "delivery|go|fly|buy") String product,
            @Pattern(regexp = "business|partner|eligibility|callback|other") String kind,
            @Size(max = 120) String company,
            @NotBlank @Size(max = 120) String contactName,
            @NotBlank @Size(max = 30) String phone,
            @Email @Size(max = 160) String email,
            @Size(max = 60) String city,
            @Size(max = 120) String activity,
            @Pattern(regexp = "|1-20|20-100|100-500|500\\+") String volume,
            @Size(max = 2000) String message,
            @Pattern(regexp = "fr|en") String lang,
            /** Champ piège invisible : rempli seulement par les robots. */
            String website) {}

    record Created(UUID id) {}

    @PostMapping("/v1/contact-requests")
    ResponseEntity<Created> submit(@Valid @RequestBody Body b, HttpServletRequest http) {
        if (b.website() != null && !b.website().isBlank()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Created(UUID.randomUUID())); // robot : ignoré sans le dire
        }
        ContactRequest r = service.submit(new ContactService.Input(b.product(), b.kind() == null ? "business" : b.kind(),
                b.company(), b.contactName(), b.phone(), b.email(), b.city(), b.activity(), b.volume(), b.message(), b.lang()),
                http.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(r.getId()));
    }

    record View(UUID id, Instant createdAt, String product, String kind, String company, String contactName, String phone,
                String email, String city, String activity, String volume, String message, String status) {}

    @GetMapping("/v1/contact-requests")
    @PreAuthorize("hasAnyRole('admin','operateur','conseiller')")
    List<View> latest(@RequestParam(defaultValue = "100") int limit) {
        return service.latest(Math.clamp(limit, 1, 500)).stream().map(r -> new View(r.getId(), r.getCreatedAt(), r.getProduct(),
                r.getKind(), r.getCompany(), r.getContactName(), r.getPhone(), r.getEmail(), r.getCity(), r.getActivity(),
                r.getVolume(), r.getMessage(), r.getStatus())).toList();
    }
}
