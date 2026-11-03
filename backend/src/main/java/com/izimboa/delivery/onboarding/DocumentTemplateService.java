package com.izimboa.delivery.onboarding;

import com.izimboa.delivery.DeliveryProblem;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Modèle de documents demandés aux livreurs, réglé par l'administration (décision 74) : nom, code, pages,
 * obligatoire ou non, numéro, date de validité. Un document déjà fourni par des livreurs n'est jamais supprimé :
 * on le désactive.
 */
@Service
class DocumentTemplateService {

    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z0-9_]{1,39}");

    private final DocumentTypeRepository types;
    private final Clock clock;

    DocumentTemplateService(DocumentTypeRepository types, Clock clock) {
        this.types = types;
        this.clock = clock;
    }

    record PageInput(Integer id, String name, String camera) {}

    record TypeInput(String code, String name, String help, Boolean mandatory, String numberRequirement,
                     Boolean expiryRequired, Boolean active, Integer position, List<PageInput> pages) {}

    @Transactional(readOnly = true)
    List<OnboardingViews.DocumentTypeView> list() {
        return types.findAllByOrderByPositionAscIdAsc().stream().map(OnboardingViews::type).toList();
    }

    @Transactional
    OnboardingViews.DocumentTypeView create(TypeInput in) {
        String code = in.code() == null ? "" : in.code().strip().toUpperCase();
        if (!CODE.matcher(code).matches()) {
            throw invalid("Code invalide : lettres majuscules, chiffres et _ (ex. PERMIS_CONDUIRE).");
        }
        if (types.findByCode(code).isPresent()) throw invalid("Ce code existe déjà.");
        var t = new DocumentType(code, clock.instant());
        apply(t, in);
        return OnboardingViews.type(types.save(t));
    }

    @Transactional
    OnboardingViews.DocumentTypeView update(int id, TypeInput in) {
        var t = types.findById(id).orElseThrow(() -> DeliveryProblem.of(HttpStatus.NOT_FOUND, "document-type-not-found", "Document inconnu."));
        apply(t, in);
        return OnboardingViews.type(types.saveAndFlush(t));
    }

    private void apply(DocumentType t, TypeInput in) {
        String name = OnboardingService.clean(in.name(), 80);
        if (name == null) throw invalid("Indiquez le nom du document.");
        DocumentType.NumberRequirement number;
        try {
            number = DocumentType.NumberRequirement.valueOf(in.numberRequirement() == null ? "NONE" : in.numberRequirement());
        } catch (IllegalArgumentException e) {
            throw invalid("Réglage du numéro inconnu.");
        }
        if (in.pages() == null || in.pages().isEmpty()) throw invalid("Ajoutez au moins une page à photographier.");
        if (in.pages().size() > 6) throw invalid("Six pages au plus par document.");
        var seen = new HashSet<String>();
        var specs = in.pages().stream().map(p -> {
            String pageName = OnboardingService.clean(p.name(), 60);
            if (pageName == null) throw invalid("Chaque page doit avoir un nom (ex. Recto, Verso).");
            if (!seen.add(pageName.toLowerCase())) throw invalid("Deux pages portent le même nom : " + pageName + ".");
            DocumentType.Camera camera = "FRONT".equals(p.camera()) ? DocumentType.Camera.FRONT : DocumentType.Camera.BACK;
            return new DocumentType.PageSpec(p.id(), pageName, camera);
        }).toList();
        t.update(name, OnboardingService.clean(in.help(), 300), Boolean.TRUE.equals(in.mandatory()), number,
                Boolean.TRUE.equals(in.expiryRequired()), in.active() == null || in.active(),
                in.position() == null ? 0 : in.position(), specs, clock.instant());
    }

    private static DeliveryProblem invalid(String message) {
        return DeliveryProblem.of(HttpStatus.UNPROCESSABLE_CONTENT, "document-type-invalid", message);
    }
}
