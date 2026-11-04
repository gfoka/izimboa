package com.izimboa.delivery.onboarding;

import com.izimboa.common.CurrentUser;
import com.izimboa.common.IdentityAdmin;
import com.izimboa.common.Roles;
import com.izimboa.delivery.DeliveryProblem;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dossier du livreur (décision 74) : il se connecte par WhatsApp, donne son prénom et son nom, photographie
 * les documents du modèle, puis l'équipe valide. La validation donne le rôle « livreur » dans Keycloak.
 */
@Service
public class OnboardingService {

    static final int MAX_IMAGE_BYTES = 6 * 1024 * 1024;
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private final CourierProfileRepository profiles;
    private final CourierDocumentRepository documents;
    private final DocumentImageRepository images;
    private final DocumentTypeRepository types;
    private final IdentityAdmin identity;
    private final Clock clock;
    private final com.izimboa.delivery.courier.CourierService couriers;
    private final com.izimboa.delivery.core.DeliveryLookup deliveries;

    OnboardingService(CourierProfileRepository profiles, CourierDocumentRepository documents, DocumentImageRepository images,
                      DocumentTypeRepository types, IdentityAdmin identity, Clock clock,
                      com.izimboa.delivery.courier.CourierService couriers,
                      com.izimboa.delivery.core.DeliveryLookup deliveries) {
        this.couriers = couriers;
        this.deliveries = deliveries;
        this.profiles = profiles;
        this.documents = documents;
        this.images = images;
        this.types = types;
        this.identity = identity;
        this.clock = clock;
    }

    // ---- Côté livreur ----

    @Transactional
    public OnboardingViews.MyFileView myFile(CurrentUser user) {
        return mine(profileOf(user));
    }

    @Transactional
    public OnboardingViews.MyFileView updateProfile(CurrentUser user, String firstName, String lastName, String email) {
        var p = profileOf(user);
        String first = clean(firstName, 60), last = clean(lastName, 60), mail = clean(email, 120);
        if (first == null || last == null) throw invalid("Indiquez votre prénom et votre nom.");
        if (mail != null && !EMAIL.matcher(mail).matches()) throw invalid("Adresse e-mail invalide.");
        identity(() -> identity.updateName(user.id(), first, last, mail));
        p.rename(first, last, mail == null ? null : mail.toLowerCase(), user.phone(), clock.instant());
        return mine(p);
    }

    public record Upload(String number, LocalDate expiresOn, Map<Integer, Image> pages) {}

    public record Image(String contentType, byte[] data) {}

    /** Envoi (ou remplacement) d'un document : toutes ses pages d'un coup, avec son numéro et sa date de validité. */
    @Transactional
    public OnboardingViews.MyFileView upload(CurrentUser user, String code, Upload in) {
        var p = profileOf(user);
        if (p.getStatus() == CourierProfile.Status.SUSPENDED) throw invalid("Compte suspendu : contactez Izimboa.");
        var t = types.findByCode(code).filter(DocumentType::isActive)
                .orElseThrow(() -> DeliveryProblem.of(HttpStatus.NOT_FOUND, "document-type-not-found", "Document inconnu."));
        String number = clean(in.number(), 60);
        if (t.getNumberRequirement() == DocumentType.NumberRequirement.REQUIRED && number == null) {
            throw invalid("Indiquez le numéro du document.");
        }
        if (t.getNumberRequirement() == DocumentType.NumberRequirement.NONE) number = null;
        LocalDate expires = t.isExpiryRequired() ? in.expiresOn() : null;
        if (t.isExpiryRequired()) {
            if (expires == null) throw invalid("Indiquez la date d'expiration du document.");
            if (expires.isBefore(LocalDate.now(clock.withZone(OnboardingViews.ZONE)))) throw invalid("Ce document est expiré.");
        }
        for (var page : t.getPages()) {
            var img = in.pages().get(page.getId());
            if (img == null) throw invalid("Photo manquante : " + page.getName() + ".");
            checkImage(img);
        }
        var now = clock.instant();
        var doc = documents.findByUserIdAndTypeId(user.id(), t.getId()).orElseGet(() -> new CourierDocument(user.id(), t.getId()));
        doc.replace(number, expires, now);
        documents.saveAndFlush(doc);
        images.deleteByDocumentId(doc.getId());
        for (var page : t.getPages()) {
            var img = in.pages().get(page.getId());
            images.save(new CourierDocument.Image(doc.getId(), page.getId(), img.contentType(), img.data(), now));
        }
        p.touch(now);
        return mine(p);
    }

    /** Le livreur envoie son dossier complet pour vérification. */
    @Transactional
    public OnboardingViews.MyFileView submit(CurrentUser user) {
        var p = profileOf(user);
        var a = assess(p);
        if (!a.complete()) throw invalid("Dossier incomplet : il manque encore des informations ou des documents.");
        if (p.getStatus() == CourierProfile.Status.SUSPENDED) throw invalid("Compte suspendu : contactez Izimboa.");
        p.submit(clock.instant());
        return mine(p);
    }

    /** Avant de passer en ligne : compte validé, documents à jour (ou délai de grâce en cours). */
    @Transactional
    public void requireCanWork(CurrentUser user) {
        var p = profileOf(user);
        if (assess(p).canWork()) return;
        throw DeliveryProblem.of(HttpStatus.CONFLICT, "courier-file-incomplete",
                "Complétez votre dossier (identité et documents) pour recevoir des courses.");
    }

    // ---- Côté équipe (validateurs) ----

    @Transactional(readOnly = true)
    public List<OnboardingViews.ProfileSummary> list(String status) {
        List<CourierProfile> rows;
        if ("TO_REVIEW".equals(status)) {
            var ids = documents.usersWithDocumentsToReview();
            rows = profiles.findAllById(ids).stream()
                    .filter(p -> p.getStatus() == CourierProfile.Status.ACTIVE || p.getStatus() == CourierProfile.Status.PENDING)
                    .toList();
        } else if (status == null || status.isBlank() || "ALL".equals(status)) {
            rows = profiles.findTop200ByOrderByUpdatedAtDesc();
        } else {
            rows = profiles.findTop200ByStatusOrderByUpdatedAtDesc(parseStatus(status));
        }
        var allTypes = types.findAllByOrderByPositionAscIdAsc();
        var stats = deliveries.ratingsByCourier(rows.stream().map(CourierProfile::getUserId).toList());
        return rows.stream().map(p -> {
            var a = assess(p, allTypes);
            var r = stats.get(p.getUserId());
            return new OnboardingViews.ProfileSummary(p.getUserId(), p.displayName(), p.getPhone(), p.getStatus().name(),
                    p.getSubmittedAt(), p.getUpdatedAt(), documents.existsByUserIdAndReviewedFalse(p.getUserId()), a.missing(),
                    r == null ? null : r.average(), r == null ? 0 : r.count());
        }).toList();
    }

    @Transactional(readOnly = true)
    public OnboardingViews.ProfileDetail detail(UUID userId) {
        var p = profiles.findById(userId).orElseThrow(OnboardingService::notFound);
        var a = assess(p);
        var rating = deliveries.ratingsByCourier(List.of(userId)).get(userId);
        var warnings = new java.util.ArrayList<String>();
        var byType = types.findAllByOrderByPositionAscIdAsc().stream().collect(Collectors.toMap(DocumentType::getId, t -> t));
        for (var d : documents.findByUserId(userId)) {
            if (d.getNumber() == null) continue;
            var others = documents.findByTypeIdAndNumberIgnoreCaseAndUserIdNot(d.getTypeId(), d.getNumber(), userId);
            if (!others.isEmpty()) {
                var t = byType.get(d.getTypeId());
                warnings.add("Le numéro " + d.getNumber() + " (" + (t == null ? "document" : t.getName())
                        + ") est aussi déclaré par " + others.size() + " autre(s) compte(s).");
            }
        }
        return new OnboardingViews.ProfileDetail(p.getUserId(), p.getFirstName(), p.getLastName(), p.getEmail(), p.getPhone(),
                p.getStatus().name(), p.getStatusReason(), p.getSubmittedAt(), p.getReviewedAt(), p.getCreatedAt(),
                a.documents(), warnings, a.complete(), a.canWork(),
                rating == null ? null : rating.average(), rating == null ? 0 : rating.count());
    }

    @Transactional(readOnly = true)
    public Image image(UUID userId, UUID documentId, int pageId) {
        var d = documents.findById(documentId).filter(x -> x.getUserId().equals(userId)).orElseThrow(OnboardingService::notFound);
        var img = images.findById(new CourierDocument.Image.Key(d.getId(), pageId)).orElseThrow(OnboardingService::notFound);
        return new Image(img.getContentType(), img.getData());
    }

    /** Valider : le compte devient livreur (ou reste livreur, documents marqués vérifiés). */
    @Transactional
    public OnboardingViews.ProfileDetail activate(UUID userId, CurrentUser staff) {
        var p = profiles.findById(userId).orElseThrow(OnboardingService::notFound);
        if (!assess(p).complete()) throw invalid("Dossier incomplet : impossible de valider.");
        identity(() -> identity.addRole(userId, Roles.LIVREUR));
        documents.findByUserId(userId).forEach(CourierDocument::markReviewed);
        p.decide(CourierProfile.Status.ACTIVE, null, staff.id(), clock.instant());
        return detail(userId);
    }

    /** Refuser (dossier à corriger) ou suspendre : le rôle livreur est retiré. Motif obligatoire, montré au livreur. */
    @Transactional
    public OnboardingViews.ProfileDetail refuse(UUID userId, boolean suspend, String reason, CurrentUser staff) {
        var p = profiles.findById(userId).orElseThrow(OnboardingService::notFound);
        String why = clean(reason, 300);
        if (why == null) throw invalid("Indiquez le motif : il sera montré au livreur.");
        identity(() -> identity.removeRole(userId, Roles.LIVREUR));
        p.decide(suspend ? CourierProfile.Status.SUSPENDED : CourierProfile.Status.REJECTED, why, staff.id(), clock.instant());
        couriers.goOffline(userId);   // plus aucune course proposée, tout de suite
        return detail(userId);
    }

    // ---- Interne ----

    /** Un échec de Keycloak annule le changement (rien n'est enregistré à moitié). */
    private static void identity(Runnable call) {
        try {
            call.run();
        } catch (IdentityAdmin.IdentityException e) {
            throw DeliveryProblem.of(HttpStatus.valueOf(e.status()), "identity", e.getMessage());
        } catch (RuntimeException e) {
            throw DeliveryProblem.of(HttpStatus.SERVICE_UNAVAILABLE, "identity-unavailable",
                    "Gestion des comptes momentanément indisponible. Réessayez dans un instant.");
        }
    }

    private CourierProfile profileOf(CurrentUser user) {
        return profiles.findById(user.id()).orElseGet(() ->
                profiles.save(new CourierProfile(user.id(), user.phone(), user.hasRole(Roles.LIVREUR), clock.instant())));
    }

    private OnboardingViews.MyFileView mine(CourierProfile p) {
        var a = assess(p);
        return new OnboardingViews.MyFileView(p.getStatus().name(), p.getStatusReason(), p.getFirstName(), p.getLastName(),
                p.getEmail(), p.getPhone(), a.documents(), a.complete(), a.canWork(), a.deadline());
    }

    private OnboardingViews.Assessment assess(CourierProfile p) {
        return assess(p, types.findAllByOrderByPositionAscIdAsc());
    }

    private OnboardingViews.Assessment assess(CourierProfile p, List<DocumentType> allTypes) {
        Map<Integer, CourierDocument> byType = new HashMap<>();
        Map<UUID, List<Integer>> pages = new HashMap<>();
        for (var d : documents.findByUserId(p.getUserId())) {
            byType.put(d.getTypeId(), d);
            pages.put(d.getId(), images.pageIds(d.getId()));
        }
        return OnboardingViews.assess(p, allTypes, byType, pages, clock.instant());
    }

    /** Seules de vraies photos sont gardées (JPEG, PNG ou WebP, d'après leur contenu). */
    static void checkImage(Image img) {
        byte[] b = img.data();
        if (b == null || b.length < 12) throw invalid("Photo illisible : reprenez-la.");
        if (b.length > MAX_IMAGE_BYTES) throw invalid("Photo trop lourde (6 Mo au plus).");
        boolean jpeg = (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8;
        boolean png = (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
        boolean webp = b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        if (!jpeg && !png && !webp) throw invalid("Format de photo non pris en charge (JPEG, PNG ou WebP).");
    }

    private static CourierProfile.Status parseStatus(String s) {
        try {
            return CourierProfile.Status.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw invalid("Statut inconnu : " + s);
        }
    }

    static String clean(String s, int max) {
        if (s == null) return null;
        String v = s.replaceAll("\\s+", " ").strip();
        if (v.isEmpty()) return null;
        return v.length() > max ? v.substring(0, max) : v;
    }

    static DeliveryProblem invalid(String message) {
        return DeliveryProblem.of(HttpStatus.UNPROCESSABLE_CONTENT, "courier-file-invalid", message);
    }

    static DeliveryProblem notFound() {
        return DeliveryProblem.of(HttpStatus.NOT_FOUND, "courier-file-not-found", "Dossier introuvable.");
    }
}
