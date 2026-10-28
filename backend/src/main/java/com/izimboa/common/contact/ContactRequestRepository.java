package com.izimboa.common.contact;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface ContactRequestRepository extends JpaRepository<ContactRequest, UUID> {
    List<ContactRequest> findAllByOrderByCreatedAtDesc(Pageable page);
}
