package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface HotspotRepository extends JpaRepository<VisualHotspot, Long> {
    @Query("SELECT h FROM VisualHotspot h WHERE h.visualContent.id = :visualContentId AND h.active = true ORDER BY h.id ASC")
    List<VisualHotspot> findByVisualContentIdAndActiveTrueOrderByIdAsc(@org.springframework.data.repository.query.Param("visualContentId") Long visualContentId);
}
