package com.wolfe.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CustomerAccessSecurityTest {
    @Test void customerCannotUseAnotherCustomerId() {
        var a = new UsernamePasswordAuthenticationToken("a@example.com", null, List.of());
        a.setDetails(10L);
        assertThrows(AccessDeniedException.class, () -> CustomerAccess.requireCustomer(a, 11L));
    }

    @Test void matchingCustomerIdIsAccepted() {
        var a = new UsernamePasswordAuthenticationToken("a@example.com", null, List.of());
        a.setDetails(10L);
        assertEquals(10L, CustomerAccess.requireCustomer(a, 10L));
    }
}
