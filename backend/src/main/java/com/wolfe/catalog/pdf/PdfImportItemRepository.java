package com.wolfe.catalog.pdf;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdfImportItemRepository extends JpaRepository<PdfImportItem, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select i from PdfImportItem i where i.id = :id")
    java.util.Optional<PdfImportItem> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    List<PdfImportItem> findByJobId(Long jobId);
    List<PdfImportItem> findByJobIdOrderByPageNumberAscIdAsc(Long jobId);
    List<PdfImportItem> findByJobIdAndStatus(Long jobId, String status);
    List<PdfImportItem> findByStatusOrderByCreatedAtDesc(String status);
    void deleteByJobId(Long jobId);
}
