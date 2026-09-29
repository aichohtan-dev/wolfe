package com.wolfe.catalog.pdf;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdfImportJobRepository extends JpaRepository<PdfImportJob, Long> {
    List<PdfImportJob> findAllByOrderByCreatedAtDesc();
}
