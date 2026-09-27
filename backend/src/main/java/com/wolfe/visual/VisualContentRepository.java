package com.wolfe.visual;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisualContentRepository extends JpaRepository<VisualContent, Long> {
    List<VisualContent> findByPlacementAndActiveTrueOrderBySortOrderAscIdAsc(String placement);
    List<VisualContent> findAllByOrderByPlacementAscSortOrderAscIdAsc();
}
