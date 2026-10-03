package com.wolfe.bundle;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BundleItemRepository extends JpaRepository<BundleItem, Long> {
    List<BundleItem> findByBundleId(Long bundleId);
    List<BundleItem> findByBundleIdIn(java.util.Collection<Long> bundleIds);
    List<BundleItem> findByProductId(Long productId);
    void deleteByProductId(Long productId);
    void deleteByBundleId(Long bundleId);
}
