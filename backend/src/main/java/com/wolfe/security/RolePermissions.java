package com.wolfe.security;

import java.util.*;

public final class RolePermissions {
    private RolePermissions() {}
    private static final Map<String, Set<String>> MAP = Map.of(
        "CUSTOMER", Set.of("CUSTOMER_SELF"),
        "RETAILER", Set.of("RETAILER_OPERATIONS"),
        "ADMIN", Set.of("ADMIN_CATALOG", "ADMIN_ORDERS", "ADMIN_CUSTOMERS_READ", "ADMIN_RETURNS", "ADMIN_RETAILER"),
        "SUPER_ADMIN", Set.of("ADMIN_CATALOG", "ADMIN_ORDERS", "ADMIN_CUSTOMERS_READ", "ADMIN_RETURNS", "ADMIN_RETAILER", "ADMIN_SECURITY", "ADMIN_AUDIT", "ADMIN_CUSTOMER_ACCESS")
    );
    public static Set<String> forRole(String role) { return MAP.getOrDefault(role == null ? "" : role.toUpperCase(Locale.ROOT), Set.of()); }
}
