package com.wolfe.security;

import org.springframework.security.core.Authentication;

public final class CustomerAccess {
    private CustomerAccess() {
    }
    public static Long requireCustomer(Authentication a, Long id) {
        if (a == null || !(a.getDetails() instanceof Long current) || !current.equals(id)) throw new org.springframework.security.access.AccessDeniedException("Customer access denied");
        return current;
    }
}
