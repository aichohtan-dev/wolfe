package com.wolfe.customer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PasswordValidationContractTest {
    @Test
    void loginAndChangePasswordAreBoundToBcryptByteCeiling() throws Exception {
        String s = Files.readString(Path.of("src/main/java/com/wolfe/customer/CustomerController.java"));
        assertTrue(s.contains("@NotBlank @Size(max = 72) String password"));
        assertTrue(s.contains("@NotBlank @Size(min = 8, max = 72) String newPassword"));
        assertTrue(s.contains("getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72"));
    }
}
