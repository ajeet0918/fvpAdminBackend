package com.agriplatform.backend.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agriplatform.backend.auth.service.JwtService;
import com.agriplatform.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SecurityProbeController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class})
class SecurityConfigTest {
    private static final String VALID_CUSTOMER_TOKEN = "valid-customer-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String STAGING_ORIGIN = "https://staging.fvppurepick.com";
    private static final String UNKNOWN_ORIGIN = "https://attacker.example";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void publicCatalogEndpointAllowsAnonymousGetRequests() throws Exception {
        mockMvc.perform(get("/api/products/security-probe"))
                .andExpect(status().isOk())
                .andExpect(content().string("public-products"));
    }

    @Test
    void adminEndpointRejectsAnonymousRequestsWithGenericMessage() throws Exception {
        mockMvc.perform(get("/api/admin/products/security-probe"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Authentication required")))
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    void adminEndpointRejectsSalesRoleOutsideAllowedSalesAdminAreas() throws Exception {
        mockMvc.perform(get("/api/admin/products/security-probe").with(user("sales").roles("SALES")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Insufficient permissions")));
    }

    @Test
    void adminEndpointAllowsAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/products/security-probe").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string("admin-products"));
    }

    @Test
    void refundEndpointRejectsSalesRole() throws Exception {
        mockMvc.perform(post("/api/orders/1/refunds").with(user("sales").roles("SALES")))
                .andExpect(status().isForbidden());
    }

    @Test
    void refundEndpointAllowsAdminRole() throws Exception {
        mockMvc.perform(post("/api/orders/1/refunds").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string("refund-created"));
    }

    @Test
    void customerEndpointAllowsValidCustomerJwt() throws Exception {
        when(jwtService.isTokenValid(VALID_CUSTOMER_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(VALID_CUSTOMER_TOKEN)).thenReturn("customer:7");
        when(jwtService.extractRole(VALID_CUSTOMER_TOKEN)).thenReturn("CUSTOMER");

        mockMvc.perform(get("/api/customer/me/security-probe")
                        .header("Authorization", "Bearer " + VALID_CUSTOMER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("customer-account"));

        verify(jwtService).isTokenValid(VALID_CUSTOMER_TOKEN);
    }

    @Test
    void customerEndpointRejectsAdminRole() throws Exception {
        mockMvc.perform(get("/api/customer/me/security-probe").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidBearerTokenDoesNotAuthenticateRequest() throws Exception {
        when(jwtService.isTokenValid(INVALID_TOKEN)).thenReturn(false);

        mockMvc.perform(get("/api/admin/products/security-probe")
                        .header("Authorization", "Bearer " + INVALID_TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Authentication required")));
    }

    @Test
    void optionsRequestsAreAllowedForSecurityPreflight() throws Exception {
        mockMvc.perform(options("/api/admin/products/security-probe"))
                .andExpect(status().isOk());
    }

    @Test
    void corsAllowsConfiguredStagingOrigin() throws Exception {
        mockMvc.perform(options("/api/products/security-probe")
                        .header("Origin", STAGING_ORIGIN)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", STAGING_ORIGIN));
    }

    @Test
    void corsRejectsUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/products/security-probe")
                        .header("Origin", UNKNOWN_ORIGIN)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

}
