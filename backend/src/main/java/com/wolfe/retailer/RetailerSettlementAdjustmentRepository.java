package com.wolfe.retailer;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface RetailerSettlementAdjustmentRepository extends JpaRepository<RetailerSettlementAdjustment,Long>{ List<RetailerSettlementAdjustment> findBySettlementIdOrderByCreatedAtAsc(Long settlementId); boolean existsBySettlementIdAndType(Long settlementId,String type); }
