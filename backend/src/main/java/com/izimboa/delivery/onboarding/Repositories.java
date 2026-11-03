package com.izimboa.delivery.onboarding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer> {
    List<DocumentType> findAllByOrderByPositionAscIdAsc();
    Optional<DocumentType> findByCode(String code);
}

interface CourierProfileRepository extends JpaRepository<CourierProfile, UUID> {
    List<CourierProfile> findTop200ByStatusOrderByUpdatedAtDesc(CourierProfile.Status status);
    List<CourierProfile> findTop200ByOrderByUpdatedAtDesc();
}

interface CourierDocumentRepository extends JpaRepository<CourierDocument, UUID> {
    List<CourierDocument> findByUserId(UUID userId);
    Optional<CourierDocument> findByUserIdAndTypeId(UUID userId, Integer typeId);
    List<CourierDocument> findByTypeIdAndNumberIgnoreCaseAndUserIdNot(Integer typeId, String number, UUID userId);
    boolean existsByUserIdAndReviewedFalse(UUID userId);

    @Query("select distinct d.userId from CourierDocument d where d.reviewed = false")
    List<UUID> usersWithDocumentsToReview();
}

interface DocumentImageRepository extends JpaRepository<CourierDocument.Image, CourierDocument.Image.Key> {
    @Query("select i.pageId from CourierDocumentImage i where i.documentId = ?1")
    List<Integer> pageIds(UUID documentId);

    @Modifying
    @Query("delete from CourierDocumentImage i where i.documentId = ?1")
    void deleteByDocumentId(UUID documentId);
}
