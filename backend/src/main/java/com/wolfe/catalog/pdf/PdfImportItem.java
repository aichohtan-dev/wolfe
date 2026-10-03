package com.wolfe.catalog.pdf;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pdf_import_items", indexes = {
    @Index(name = "idx_pdf_items_job", columnList = "job_id, status"),
    @Index(name = "idx_pdf_items_sku", columnList = "sku")
})
public class PdfImportItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(nullable = false)
    private String name;

    @Column(length = 120)
    private String sku;

    @Column(length = 120)
    private String brand;

    @Column(length = 120)
    private String category;

    @Column(length = 120)
    private String subcategory;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String finish;

    @Column(length = 100)
    private String material;

    @Column(length = 100)
    private String color;

    @Column(length = 100)
    private String size;

    @Column(length = 200)
    private String dimensions;

    @Column(length = 2000)
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "media_urls", length = 4000)
    private String mediaUrls;

    @Column(name = "extracted_variants_json", length = 8000)
    private String extractedVariantsJson;

    @Column(name = "attributes_json", length = 8000)
    private String attributesJson;

    @Column(nullable = false, length = 50)
    private String status = "DRAFT"; // DRAFT, REVIEW_REQUIRED, APPROVED, REJECTED, MERGED

    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore = new BigDecimal("0.90");

    @Column(name = "duplicate_product_id")
    private Long duplicateProductId;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected PdfImportItem() {}

    public PdfImportItem(Long jobId, Integer pageNumber, String name, String sku, String brand, String category,
                         String subcategory, BigDecimal price, String finish, String material, String color,
                         String size, String dimensions, String description, String imageUrl, String mediaUrls,
                         String extractedVariantsJson, String attributesJson, String status, BigDecimal confidenceScore,
                         Long duplicateProductId, String notes) {
        this.jobId = jobId;
        this.pageNumber = pageNumber;
        this.name = name;
        this.sku = sku;
        this.brand = brand;
        this.category = category;
        this.subcategory = subcategory;
        this.price = price;
        this.finish = finish;
        this.material = material;
        this.color = color;
        this.size = size;
        this.dimensions = dimensions;
        this.description = description;
        this.imageUrl = imageUrl;
        this.mediaUrls = mediaUrls;
        this.extractedVariantsJson = extractedVariantsJson;
        this.attributesJson = attributesJson;
        this.status = status != null ? status : "DRAFT";
        this.confidenceScore = confidenceScore != null ? confidenceScore : new BigDecimal("0.90");
        this.duplicateProductId = duplicateProductId;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public Integer getPageNumber() { return pageNumber; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public BigDecimal getPrice() { return price; }
    public String getFinish() { return finish; }
    public String getMaterial() { return material; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public String getDimensions() { return dimensions; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getMediaUrls() { return mediaUrls; }
    public String getExtractedVariantsJson() { return extractedVariantsJson; }
    public String getAttributesJson() { return attributesJson; }
    public String getStatus() { return status; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public Long getDuplicateProductId() { return duplicateProductId; }
    public String getNotes() { return notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setMediaUrls(String mediaUrls) { this.mediaUrls = mediaUrls; }
    public void setDuplicateProductId(Long duplicateProductId) { this.duplicateProductId = duplicateProductId; }

    public void updateDetails(String name, String sku, String brand, String category, String subcategory,
                              BigDecimal price, String finish, String material, String color, String size,
                              String dimensions, String description, String imageUrl, String mediaUrls,
                              String extractedVariantsJson, String attributesJson, String notes) {
        this.name = name;
        this.sku = sku;
        this.brand = brand;
        this.category = category;
        this.subcategory = subcategory;
        this.price = price;
        this.finish = finish;
        this.material = material;
        this.color = color;
        this.size = size;
        this.dimensions = dimensions;
        this.description = description;
        this.imageUrl = imageUrl;
        this.mediaUrls = mediaUrls;
        this.extractedVariantsJson = extractedVariantsJson;
        this.attributesJson = attributesJson;
        this.notes = notes;
    }
}
