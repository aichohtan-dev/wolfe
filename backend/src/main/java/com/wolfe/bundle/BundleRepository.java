package com.wolfe.bundle;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BundleRepository extends JpaRepository<Bundle, Long> {
    Optional<Bundle> findBySlug(String slug);
}
