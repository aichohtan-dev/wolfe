package com.wolfe.consultation;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest,Long>{
    List<ConsultationRequest> findAllByOrderByCreatedAtDesc();
}
