package com.wolfe.address;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomerIdOrderByIsDefaultDescIdDesc(Long customerId);
    long countByCustomerId(Long customerId);
}
