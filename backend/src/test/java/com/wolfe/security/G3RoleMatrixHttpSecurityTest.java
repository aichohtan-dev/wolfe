package com.wolfe.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class G3RoleMatrixHttpSecurityTest {

    @Autowired
    MockMvc mvc;

    @Test
    void anonymousCannotEnterRetailerOrAdmin() throws Exception {
        mvc.perform(get("/api/v1/retailer/me"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotEnterRetailerOrAdmin() throws Exception {
        mvc.perform(get("/api/v1/retailer/me").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/admin").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void retailerCannotEnterAdminButPassesRetailerAuthorization() throws Exception {
        mvc.perform(get("/api/v1/retailer/me").with(user("retailer").roles("RETAILER")))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        403, result.getResponse().getStatus()));

        mvc.perform(get("/api/v1/admin").with(user("retailer").roles("RETAILER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanEnterAdminButCannotEnterRetailer() throws Exception {
        mvc.perform(get("/api/v1/admin").with(user("admin").roles("ADMIN")))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        403, result.getResponse().getStatus()));

        mvc.perform(get("/api/v1/retailer/me").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminCanEnterAdminButCannotEnterRetailer() throws Exception {
        mvc.perform(get("/api/v1/admin").with(user("superadmin").roles("SUPER_ADMIN")))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        403, result.getResponse().getStatus()));

        mvc.perform(get("/api/v1/retailer/me").with(user("superadmin").roles("SUPER_ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void retailerEndpointDoesNotAcceptAdminOrSuperAdmin() throws Exception {
        mvc.perform(get("/api/v1/retailer/me").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/retailer/me").with(user("superadmin").roles("SUPER_ADMIN")))
                .andExpect(status().isForbidden());
    }
}
