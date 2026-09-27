package com.wolfe.visual;

import jakarta.persistence.*;

@Entity
@Table(name = "visual_contents")
public class VisualContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String placement;
    @Column(nullable = false, length = 120) private String title;
    @Column(length = 500) private String subtitle;
    @Column(nullable = false, length = 20) private String mediaType;
    @Column(nullable = false, length = 1200) private String mediaUrl;
    @Column(length = 500) private String posterUrl;
    @Column(length = 500) private String linkUrl;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private int sortOrder = 0;
    protected VisualContent() {
    }
    public VisualContent(String placement, String title, String subtitle, String mediaType, String mediaUrl, String posterUrl, String linkUrl, boolean active,
    int sortOrder) {
        this.placement = placement;
        this.title = title;
        this.subtitle = subtitle;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.posterUrl = posterUrl;
        this.linkUrl = linkUrl;
        this.active = active;
        this.sortOrder = sortOrder;
    }
    public Long getId() {
        return id;
    }
    public String getPlacement() {
        return placement;
    }
    public String getTitle() {
        return title;
    }
    public String getSubtitle() {
        return subtitle;
    }
    public String getMediaType() {
        return mediaType;
    }
    public String getMediaUrl() {
        return mediaUrl;
    }
    public String getPosterUrl() {
        return posterUrl;
    }
    public String getLinkUrl() {
        return linkUrl;
    }
    public boolean isActive() {
        return active;
    }
    public int getSortOrder() {
        return sortOrder;
    }
    public void update(String placement, String title, String subtitle, String mediaType, String mediaUrl, String posterUrl, String linkUrl, boolean active,
    int sortOrder) {
        this.placement = placement;
        this.title = title;
        this.subtitle = subtitle;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.posterUrl = posterUrl;
        this.linkUrl = linkUrl;
        this.active = active;
        this.sortOrder = sortOrder;
    }
}
