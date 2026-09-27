package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface HotspotRepository extends JpaRepository<VisualHotspot, Long> {
    List<VisualHotspot> findByVisualContentIdAndActiveTrueOrderByIdAsc(Long visualContentId);
}
