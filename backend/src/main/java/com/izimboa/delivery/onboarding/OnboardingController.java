package com.izimboa.delivery.onboarding;

import com.izimboa.common.CurrentUser;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/** Inscription des livreurs (dossier, documents) et vérification par l'équipe Izimboa (décision 74). */
@RestController
class OnboardingController {

    private final OnboardingService onboarding;
    private final DocumentTemplateService templates;

    OnboardingController(OnboardingService onboarding, DocumentTemplateService templates) {
        this.onboarding = onboarding;
        this.templates = templates;
    }

    // ---- Le livreur (tout compte connecté : on devient livreur après validation) ----

    record ProfileRequest(String firstName, String lastName, String email) {}

    @GetMapping("/v1/delivery/onboarding")
    OnboardingViews.MyFileView mine(CurrentUser user) {
        return onboarding.myFile(user);
    }

    @PutMapping("/v1/delivery/onboarding/profile")
    OnboardingViews.MyFileView profile(@RequestBody ProfileRequest body, CurrentUser user) {
        return onboarding.updateProfile(user, body.firstName(), body.lastName(), body.email());
    }

    /**
     * Un document : champs « number », « expiresOn » (AAAA-MM-JJ) et une photo par page, nommée « page-{id} ».
     */
    @PostMapping(value = "/v1/delivery/onboarding/documents/{code}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    OnboardingViews.MyFileView upload(@PathVariable String code, MultipartHttpServletRequest request, CurrentUser user) {
        var pages = new HashMap<Integer, OnboardingService.Image>();
        for (var entry : request.getFileMap().entrySet()) {
            if (!entry.getKey().startsWith("page-")) continue;
            try {
                int pageId = Integer.parseInt(entry.getKey().substring(5));
                var f = entry.getValue();
                pages.put(pageId, new OnboardingService.Image(
                        f.getContentType() == null ? "image/jpeg" : f.getContentType(), f.getBytes()));
            } catch (NumberFormatException | IOException e) {
                throw OnboardingService.invalid("Photo illisible : reprenez-la.");
            }
        }
        LocalDate expires = null;
        String rawDate = request.getParameter("expiresOn");
        if (rawDate != null && !rawDate.isBlank()) {
            try {
                expires = LocalDate.parse(rawDate.strip());
            } catch (DateTimeParseException e) {
                throw OnboardingService.invalid("Date d'expiration invalide.");
            }
        }
        return onboarding.upload(user, code, new OnboardingService.Upload(request.getParameter("number"), expires, pages));
    }

    @PostMapping("/v1/delivery/onboarding/submit")
    OnboardingViews.MyFileView submit(CurrentUser user) {
        return onboarding.submit(user);
    }

    // ---- L'équipe : validateurs, opérateurs, administrateurs ----

    record ReasonRequest(String reason) {}

    @GetMapping("/v1/delivery/couriers")
    @PreAuthorize("hasAnyRole('validateur','operateur','admin')")
    List<OnboardingViews.ProfileSummary> list(@RequestParam(required = false) String status) {
        return onboarding.list(status);
    }

    @GetMapping("/v1/delivery/couriers/{userId}")
    @PreAuthorize("hasAnyRole('validateur','operateur','admin')")
    OnboardingViews.ProfileDetail detail(@PathVariable UUID userId) {
        return onboarding.detail(userId);
    }

    /** Photo d'une page : jamais gardée en cache (pièces d'identité). */
    @GetMapping("/v1/delivery/couriers/{userId}/documents/{documentId}/pages/{pageId}")
    @PreAuthorize("hasAnyRole('validateur','operateur','admin')")
    ResponseEntity<byte[]> image(@PathVariable UUID userId, @PathVariable UUID documentId, @PathVariable int pageId) {
        var img = onboarding.image(userId, documentId, pageId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(img.contentType())).body(img.data());
    }

    @PostMapping("/v1/delivery/couriers/{userId}/activate")
    @PreAuthorize("hasAnyRole('validateur','admin')")
    OnboardingViews.ProfileDetail activate(@PathVariable UUID userId, CurrentUser staff) {
        return onboarding.activate(userId, staff);
    }

    @PostMapping("/v1/delivery/couriers/{userId}/reject")
    @PreAuthorize("hasAnyRole('validateur','admin')")
    OnboardingViews.ProfileDetail reject(@PathVariable UUID userId, @RequestBody ReasonRequest body, CurrentUser staff) {
        return onboarding.refuse(userId, false, body.reason(), staff);
    }

    @PostMapping("/v1/delivery/couriers/{userId}/suspend")
    @PreAuthorize("hasAnyRole('validateur','admin')")
    OnboardingViews.ProfileDetail suspend(@PathVariable UUID userId, @RequestBody ReasonRequest body, CurrentUser staff) {
        return onboarding.refuse(userId, true, body.reason(), staff);
    }

    // ---- Modèle de documents : lecture pour l'équipe, modification par l'administration ----

    @GetMapping("/v1/delivery/document-types")
    @PreAuthorize("hasAnyRole('validateur','operateur','admin')")
    List<OnboardingViews.DocumentTypeView> types() {
        return templates.list();
    }

    @PostMapping("/v1/delivery/document-types")
    @PreAuthorize("hasRole('admin')")
    ResponseEntity<OnboardingViews.DocumentTypeView> create(@RequestBody DocumentTemplateService.TypeInput body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templates.create(body));
    }

    @PutMapping("/v1/delivery/document-types/{id}")
    @PreAuthorize("hasRole('admin')")
    OnboardingViews.DocumentTypeView update(@PathVariable int id, @RequestBody DocumentTemplateService.TypeInput body) {
        return templates.update(id, body);
    }

}
