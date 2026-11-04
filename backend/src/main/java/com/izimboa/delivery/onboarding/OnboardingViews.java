package com.izimboa.delivery.onboarding;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Ce que voient le livreur (son dossier) et l'équipe (dossiers à vérifier, modèle de documents). */
final class OnboardingViews {

    /** Délai laissé à un livreur déjà actif pour fournir un document nouvellement demandé. */
    static final Duration GRACE = Duration.ofDays(7);
    /** Prévenir le livreur avant l'expiration d'un document. */
    static final int EXPIRY_WARNING_DAYS = 30;
    static final ZoneId ZONE = ZoneId.of("Africa/Douala");

    private OnboardingViews() {}

    record PageView(int id, String name, String camera) {}

    record DocumentTypeView(int id, String code, String name, String help, boolean mandatory, String numberRequirement,
                            boolean expiryRequired, boolean active, int position, List<PageView> pages) {}

    record SubmittedView(String id, String number, LocalDate expiresOn, Instant submittedAt, List<Integer> pages,
                         boolean reviewed) {}

    /** state : MISSING (à fournir), EXPIRED (expiré), EXPIRING_SOON (expire bientôt), OK. */
    record RequirementView(DocumentTypeView type, SubmittedView submitted, String state) {}

    /**
     * Dossier du livreur. {@code canWork} : il peut passer en ligne. {@code deadline} : date limite pour compléter
     * son dossier sans être bloqué (livreur déjà actif à qui on demande un nouveau document).
     */
    record MyFileView(String status, String reason, String firstName, String lastName, String email, String phone,
                      List<RequirementView> documents, boolean complete, boolean canWork, LocalDate deadline) {}

    record ProfileSummary(UUID userId, String name, String phone, String status, Instant submittedAt,
                          Instant updatedAt, boolean toReview, int missing, Double ratingAverage, long ratingCount) {}

    record ProfileDetail(UUID userId, String firstName, String lastName, String email, String phone, String status,
                         String reason, Instant submittedAt, Instant reviewedAt, Instant createdAt,
                         List<RequirementView> documents, List<String> warnings, boolean complete, boolean canWork,
                         Double ratingAverage, long ratingCount) {}

    static PageView page(DocumentType.Page p) {
        return new PageView(p.getId(), p.getName(), p.getCamera().name());
    }

    static DocumentTypeView type(DocumentType t) {
        return new DocumentTypeView(t.getId(), t.getCode(), t.getName(), t.getHelp(), t.isMandatory(),
                t.getNumberRequirement().name(), t.isExpiryRequired(), t.isActive(), t.getPosition(),
                t.getPages().stream().map(OnboardingViews::page).toList());
    }

    /** État du dossier à un instant donné : documents demandés, manquants, expirés, et si le livreur peut travailler. */
    record Assessment(List<RequirementView> documents, boolean complete, boolean canWork, LocalDate deadline,
                      int missing) {}

    static Assessment assess(CourierProfile profile, List<DocumentType> types, Map<Integer, CourierDocument> docs,
                             Map<UUID, List<Integer>> pagesByDocument, Instant now) {
        LocalDate today = LocalDate.ofInstant(now, ZONE);
        var out = new ArrayList<RequirementView>();
        boolean complete = profile.hasName();
        boolean blocked = false;
        LocalDate deadline = null;
        int missing = 0;
        for (var t : types) {
            if (!t.isActive()) continue;
            var d = docs.get(t.getId());
            List<Integer> uploaded = d == null ? List.of() : pagesByDocument.getOrDefault(d.getId(), List.of());
            boolean allPages = d != null && t.getPages().stream().allMatch(p -> uploaded.contains(p.getId()));
            String state;
            if (!allPages) state = "MISSING";
            else if (t.isExpiryRequired() && d.getExpiresOn() != null && d.getExpiresOn().isBefore(today)) state = "EXPIRED";
            else if (t.isExpiryRequired() && d.getExpiresOn() != null
                    && !d.getExpiresOn().isAfter(today.plusDays(EXPIRY_WARNING_DAYS))) state = "EXPIRING_SOON";
            else state = "OK";
            var submitted = d == null ? null
                    : new SubmittedView(d.getId().toString(), d.getNumber(), d.getExpiresOn(), d.getSubmittedAt(), uploaded, d.isReviewed());
            out.add(new RequirementView(type(t), submitted, state));
            if (!t.isMandatory()) continue;
            if (state.equals("MISSING") || state.equals("EXPIRED")) {
                complete = false;
                missing++;
            }
            if (state.equals("EXPIRED")) blocked = true;
            if (state.equals("MISSING")) {
                Instant from = t.getCreatedAt().isAfter(profile.getCreatedAt()) ? t.getCreatedAt() : profile.getCreatedAt();
                LocalDate end = LocalDate.ofInstant(from.plus(GRACE), ZONE);
                if (!today.isBefore(end)) blocked = true;
                else if (deadline == null || end.isBefore(deadline)) deadline = end;
            }
        }
        if (!profile.hasName()) {
            LocalDate end = LocalDate.ofInstant(profile.getCreatedAt().plus(GRACE), ZONE);
            if (!today.isBefore(end)) blocked = true;
            else if (deadline == null || end.isBefore(deadline)) deadline = end;
        }
        boolean canWork = profile.getStatus() == CourierProfile.Status.ACTIVE && !blocked;
        return new Assessment(out, complete, canWork, complete ? null : deadline, missing);
    }
}
