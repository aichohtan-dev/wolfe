package com.wolfe.customer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AccountLifecycleConcurrencyContractTest {
    @Test
    void accountTokenRepositoryProvidesRowLockForOneTimeTokens() throws Exception {
        String s = Files.readString(Path.of("src/main/java/com/wolfe/customer/AccountTokenRepository.java"));
        assertTrue(s.contains("@Lock(LockModeType.PESSIMISTIC_WRITE)"));
        assertTrue(s.contains("findByTokenHashAndTypeForUpdate"));
    }

    @Test
    void lifecycleAcquiresCustomerBeforeTokenLock() throws Exception {
        String s = Files.readString(Path.of("src/main/java/com/wolfe/customer/AccountLifecycleService.java"));
        int customerLock = s.indexOf("customers.findByIdForUpdate");
        int tokenLock = s.indexOf("tokens.findByTokenHashAndTypeForUpdate");
        assertTrue(customerLock >= 0 && tokenLock > customerLock, "customer lock must precede token lock");
        int issueCustomerLock = s.indexOf("customers.findByIdForUpdate(customerId)");
        int issueDelete = s.indexOf("tokens.deleteByCustomerIdAndType", issueCustomerLock);
        assertTrue(issueCustomerLock >= 0 && issueDelete > issueCustomerLock, "token issuance must lock customer before replacing tokens");
    }
}
