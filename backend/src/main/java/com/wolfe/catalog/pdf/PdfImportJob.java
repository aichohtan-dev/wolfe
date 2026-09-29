package com.wolfe.catalog.pdf;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pdf_import_jobs", indexes = {
    @Index(name = "idx_pdf_jobs_status", columnList = "status")
})
public class PdfImportJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, PROCESSING, EXTRACTING, MATCHING, REVIEW_REQUIRED, COMPLETED, FAILED

    @Column(name = "total_pages")
    private Integer totalPages = 0;

    @Column(name = "total_extracted")
    private Integer totalExtracted = 0;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected PdfImportJob() {}

    public PdfImportJob(String fileName, String filePath, Long fileSize) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.status = "PENDING";
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public Long getFileSize() { return fileSize; }
    public String getStatus() { return status; }
    public Integer getTotalPages() { return totalPages; }
    public Integer getTotalExtracted() { return totalExtracted; }
    public String getErrorMessage() { return errorMessage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public void setTotalExtracted(Integer totalExtracted) { this.totalExtracted = totalExtracted; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
