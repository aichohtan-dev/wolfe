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

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "default_brand_id")
    private Long defaultBrandId;

    @Column(name = "default_category_id")
    private Long defaultCategoryId;

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
    public Long getDefaultBrandId() { return defaultBrandId; }
    public Long getDefaultCategoryId() { return defaultCategoryId; }
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

    public void setDefaultBrandId(Long id) { this.defaultBrandId = id; this.updatedAt = OffsetDateTime.now(); }
    public void setFilePath(String filePath) { this.filePath = filePath; this.updatedAt = OffsetDateTime.now(); }
    public void setDefaultCategoryId(Long id) { this.defaultCategoryId = id; this.updatedAt = OffsetDateTime.now(); }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; this.updatedAt = OffsetDateTime.now(); }
    public void setTotalExtracted(Integer totalExtracted) { this.totalExtracted = totalExtracted; this.updatedAt = OffsetDateTime.now(); }
    public void heartbeat() { this.updatedAt = OffsetDateTime.now(); }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; this.updatedAt = OffsetDateTime.now(); }
}
