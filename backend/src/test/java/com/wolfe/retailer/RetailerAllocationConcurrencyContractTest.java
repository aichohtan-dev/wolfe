package com.wolfe.retailer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RetailerAllocationConcurrencyContractTest {
    @Test
    void staleSlaCandidatesAreRelockedBeforeReleasingRetailerStock() throws Exception {
        String repo = Files.readString(Path.of("src/main/java/com/wolfe/retailer/RetailerOrderAssignmentRepository.java"));
        String service = Files.readString(Path.of("src/main/java/com/wolfe/retailer/RetailerAllocationService.java"));
        assertTrue(repo.contains("findByIdForUpdate"));
        int candidate = service.indexOf("findByStatusAndAssignedAtBefore");
        int orderLock = service.indexOf("orderRepo.findByIdForUpdate", candidate);
        int assignmentLock = service.indexOf("assignmentRepo.findByIdForUpdate", orderLock);
        int release = service.indexOf("inventoryService.releaseStock", assignmentLock);
        assertTrue(candidate >= 0 && orderLock > candidate && assignmentLock > orderLock && release > assignmentLock);
        assertTrue(service.contains("!\"ASSIGNED\".equalsIgnoreCase(assignment.getStatus())"));
    }
}
