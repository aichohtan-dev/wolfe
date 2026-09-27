package com.wolfe.visual;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/visual-content")
public class VisualContentController {
    private final VisualContentRepository repo;
    public VisualContentController(VisualContentRepository repo) {
        this.repo = repo;
    }
    @GetMapping public List<VisualContent> list(@RequestParam(defaultValue = "HERO") String placement) {
        return repo.findByPlacementAndActiveTrueOrderBySortOrderAscIdAsc(placement.trim().toUpperCase());
    }
}
