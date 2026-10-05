package com.wolfe.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class G3RoleMatrixHttpSecurityTest {
    /*
     * These probe paths are intentionally unmapped. A 404 proves the request
     * passed URL-level authorization; a 403 proves it was blocked by the
     * SecurityConfig matcher before controller mapping.
     */
    @Test
    void anonymousCannotEnterRetailerOrAdmin() throws Exception {
        mvc.perform(get("/api/v1/retailer/__g3_authorization_probe__"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/__g3_authorization_probe__"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotEnterRetailerOrAdmin() throws Exception {
        mvc.perform(get("/api/v1/retailer/__g3_authorization_probe__")
                        .with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/__g3_authorization_probe__")
                        .with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void retailerCanPassRetailerMatcherButCannotPassAdminMatcher() throws Exception {
        mvc.perform(get("/api/v1/retailer/__g3_authorization_probe__")
                        .with(user("retailer").roles("RETAILER")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/admin/__g3_authorization_probe__")
                        .with(user("retailer").roles("RETAILER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanPassAdminMatcherButCannotPassRetailerMatcher() throws Exception {
        mvc.perform(get("/api/v1/admin/__g3_authorization_probe__")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/retailer/__g3_authorization_probe__")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminCanPassAdminMatcherButCannotPassRetailerMatcher() throws Exception {
        mvc.perform(get("/api/v1/admin/__g3_authorization_probe__")
                        .with(user("superadmin").roles("SUPER_ADMIN")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/retailer/__g3_authorization_probe__")
                        .with(user("superadmin").roles("SUPER_ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Autowired
    MockMvc mvc;
}
