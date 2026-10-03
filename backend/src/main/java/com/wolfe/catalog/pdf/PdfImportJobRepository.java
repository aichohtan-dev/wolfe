package com.wolfe.catalog.pdf;

import java.util.List;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PdfImportJobRepository extends JpaRepository<PdfImportJob, Long> {
    List<PdfImportJob> findAllByOrderByCreatedAtDesc();
    List<PdfImportJob> findByStatusInAndUpdatedAtBefore(List<String> statuses, OffsetDateTime cutoff);

    @Transactional
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true, clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update PdfImportJob j set j.status = 'PROCESSING', j.updatedAt = :now where j.id = :id and j.status = 'QUEUED'")
    int claimQueuedForProcessing(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("now") OffsetDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select j from PdfImportJob j where j.id = :id")
    Optional<PdfImportJob> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
