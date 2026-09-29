package com.wolfe.retailer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerServiceAreaRepository extends JpaRepository<RetailerServiceArea, Long> {
    List<RetailerServiceArea> findByRetailerId(Long retailerId);
    List<RetailerServiceArea> findByPincodeAndActiveTrue(String pincode);
    List<RetailerServiceArea> findByCityIgnoreCaseAndActiveTrue(String city);
    Optional<RetailerServiceArea> findByRetailerIdAndPincode(Long retailerId, String pincode);
}
