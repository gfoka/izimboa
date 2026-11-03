package com.izimboa.delivery.onboarding;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Un document demandé aux livreurs, paramétré par l'administration (modèle de documents). */
@Entity
@Table(name = "kyc_document_type", schema = "delivery")
class DocumentType {

    enum NumberRequirement { NONE, OPTIONAL, REQUIRED }

    enum Camera { BACK, FRONT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(nullable = false, length = 40) private String code;
    @Column(nullable = false, length = 80) private String name;
    @Column(length = 300) private String help;
    @Column(nullable = false) private boolean mandatory;
    @Enumerated(EnumType.STRING) @Column(name = "number_requirement", nullable = false, length = 10)
    private NumberRequirement numberRequirement;
    @Column(name = "expiry_required", nullable = false) private boolean expiryRequired;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private int position;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position")
    private List<Page> pages = new ArrayList<>();

    /** Une page à photographier (« Recto », « Verso », « Photo de vous »). */
    @Entity(name = "KycDocumentPage")
    @Table(name = "kyc_document_page", schema = "delivery")
    static class Page {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "type_id", nullable = false) private DocumentType type;
        @Column(nullable = false) private int position;
        @Column(nullable = false, length = 60) private String name;
        @Enumerated(EnumType.STRING) @Column(nullable = false, length = 5) private Camera camera;

        protected Page() {}

        Page(DocumentType type, int position, String name, Camera camera) {
            this.type = type;
            this.position = position;
            this.name = name;
            this.camera = camera;
        }

        Integer getId() { return id; }
        int getPosition() { return position; }
        String getName() { return name; }
        Camera getCamera() { return camera; }
        void rename(String name, Camera camera, int position) { this.name = name; this.camera = camera; this.position = position; }
    }

    record PageSpec(Integer id, String name, Camera camera) {}

    protected DocumentType() {}

    DocumentType(String code, Instant now) {
        this.code = code;
        this.createdAt = now;
    }

    /** Mise à jour complète ; les pages gardent leur identifiant (les photos déjà envoyées restent liées). */
    void update(String name, String help, boolean mandatory, NumberRequirement number, boolean expiryRequired,
                boolean active, int position, List<PageSpec> specs, Instant now) {
        this.name = name;
        this.help = help;
        this.mandatory = mandatory;
        this.numberRequirement = number;
        this.expiryRequired = expiryRequired;
        this.active = active;
        this.position = position;
        this.updatedAt = now;
        var kept = new ArrayList<Page>();
        int i = 1;
        for (var spec : specs) {
            Page page = spec.id() == null ? null
                    : pages.stream().filter(p -> spec.id().equals(p.getId())).findFirst().orElse(null);
            if (page == null) page = new Page(this, i, spec.name(), spec.camera());
            else page.rename(spec.name(), spec.camera(), i);
            kept.add(page);
            i++;
        }
        pages.retainAll(kept);
        for (var p : kept) if (!pages.contains(p)) pages.add(p);
    }

    Integer getId() { return id; }
    String getCode() { return code; }
    String getName() { return name; }
    String getHelp() { return help; }
    boolean isMandatory() { return mandatory; }
    NumberRequirement getNumberRequirement() { return numberRequirement; }
    boolean isExpiryRequired() { return expiryRequired; }
    boolean isActive() { return active; }
    int getPosition() { return position; }
    Instant getCreatedAt() { return createdAt; }
    List<Page> getPages() { return pages; }
}
