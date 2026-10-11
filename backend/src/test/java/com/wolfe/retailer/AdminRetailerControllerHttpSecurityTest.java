package com.wolfe.retailer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class AdminRetailerControllerHttpSecurityTest {
    @Autowired MockMvc mvc;

    @Test
    void anonymousCannotReadMappedAdminRetailerEndpoint() throws Exception {
        mvc.perform(get("/api/v1/admin/retailers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotReadMappedAdminRetailerEndpoint() throws Exception {
        mvc.perform(get("/api/v1/admin/retailers")
                        .with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoleWithoutExplicitRetailerPermissionCannotReadMappedEndpoint() throws Exception {
        mvc.perform(get("/api/v1/admin/retailers")
                        .with(user("admin").authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }
}
