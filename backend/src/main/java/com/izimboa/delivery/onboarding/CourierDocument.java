package com.izimboa.delivery.onboarding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Un document fourni par un livreur ; un nouvel envoi du même document remplace le précédent. */
@Entity
@Table(name = "courier_document", schema = "delivery")
class CourierDocument {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "type_id", nullable = false) private Integer typeId;
    @Column(length = 60) private String number;
    @Column(name = "expires_on") private LocalDate expiresOn;
    @Column(nullable = false) private boolean reviewed;
    @Column(name = "submitted_at", nullable = false) private Instant submittedAt;

    protected CourierDocument() {}

    CourierDocument(UUID userId, Integer typeId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.typeId = typeId;
    }

    void replace(String number, LocalDate expiresOn, Instant now) {
        this.number = number;
        this.expiresOn = expiresOn;
        this.reviewed = false;
        this.submittedAt = now;
    }

    void markReviewed() { this.reviewed = true; }

    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    Integer getTypeId() { return typeId; }
    String getNumber() { return number; }
    LocalDate getExpiresOn() { return expiresOn; }
    boolean isReviewed() { return reviewed; }
    Instant getSubmittedAt() { return submittedAt; }

    /** Photo d'une page du document (JPEG réduit par l'application). */
    @Entity(name = "CourierDocumentImage")
    @Table(name = "courier_document_image", schema = "delivery")
    @IdClass(Image.Key.class)
    static class Image {
        static class Key implements Serializable {
            UUID documentId;
            Integer pageId;

            Key() {}

            Key(UUID documentId, Integer pageId) { this.documentId = documentId; this.pageId = pageId; }

            @Override public boolean equals(Object o) {
                return o instanceof Key k && java.util.Objects.equals(documentId, k.documentId) && java.util.Objects.equals(pageId, k.pageId);
            }

            @Override public int hashCode() { return java.util.Objects.hash(documentId, pageId); }
        }

        @Id @Column(name = "document_id") private UUID documentId;
        @Id @Column(name = "page_id") private Integer pageId;
        @Column(name = "content_type", nullable = false, length = 30) private String contentType;
        @Column(nullable = false) private byte[] data;
        @Column(name = "uploaded_at", nullable = false) private Instant uploadedAt;

        protected Image() {}

        Image(UUID documentId, Integer pageId, String contentType, byte[] data, Instant now) {
            this.documentId = documentId;
            this.pageId = pageId;
            this.contentType = contentType;
            this.data = data;
            this.uploadedAt = now;
        }

        UUID getDocumentId() { return documentId; }
        Integer getPageId() { return pageId; }
        String getContentType() { return contentType; }
        byte[] getData() { return data; }
    }
}
