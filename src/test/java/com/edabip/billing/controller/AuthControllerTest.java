package com.edabip.billing.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.edabip.billing.model.TokenResponse;
import com.edabip.billing.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock AuthService service;
    @InjectMocks AuthController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() { mvc = MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    void loginReturnsTokenResponse() throws Exception {
        when(service.login("demo@example.com", "demo123"))
                .thenReturn(new TokenResponse("jwt-value", "Bearer", 3600));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"demo@example.com","password":"demo123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-value"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

        verify(service).login("demo@example.com", "demo123");
    }
}
