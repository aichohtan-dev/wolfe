package com.wolfe.catalog.pdf;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdfImportItemRepository extends JpaRepository<PdfImportItem, Long> {
    List<PdfImportItem> findByJobId(Long jobId);
    List<PdfImportItem> findByJobIdOrderByPageNumberAscIdAsc(Long jobId);
    List<PdfImportItem> findByJobIdAndStatus(Long jobId, String status);
    List<PdfImportItem> findByStatusOrderByCreatedAtDesc(String status);
    void deleteByJobId(Long jobId);
}
