package com.wolfe.experience;

import com.wolfe.visual.VisualContent;
import jakarta.persistence.*;

@Entity
@Table(name = "visual_hotspots")
public class VisualHotspot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visual_content_id", nullable = false) private VisualContent visualContent;
    @Column(nullable = false, length = 120) private String label;
    @Column(nullable = false, length = 120) private String targetSlug;
    @Column(nullable = false) private double x = 50, y = 50;
    @Column(nullable = false) private boolean active = true;
    protected VisualHotspot() {
    }
    public VisualHotspot(VisualContent content, String label, String targetSlug, double x, double y, boolean active) {
        this.visualContent = content;
        this.label = label;
        this.targetSlug = targetSlug;
        this.x = x;
        this.y = y;
        this.active = active;
    }
    public Long getId() {
        return id;
    }
    public Long getVisualContentId() {
        return visualContent.getId();
    }
    public String getLabel() {
        return label;
    }
    public String getTargetSlug() {
        return targetSlug;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public boolean isActive() {
        return active;
    }
    public void update(String label, String targetSlug, double x, double y, boolean active) {
        this.label = label;
        this.targetSlug = targetSlug;
        this.x = x;
        this.y = y;
        this.active = active;
    }
}
