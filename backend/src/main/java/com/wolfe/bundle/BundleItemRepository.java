package com.wolfe.bundle;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BundleItemRepository extends JpaRepository<BundleItem, Long> {
    List<BundleItem> findByBundleId(Long bundleId);
    void deleteByBundleId(Long bundleId);
}
