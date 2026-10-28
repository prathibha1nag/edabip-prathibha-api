package com.edabip.billing.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.edabip.billing.model.BillingCycle;
import com.edabip.billing.model.Subscription;
import com.edabip.billing.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {
    @Mock SubscriptionService service;
    @InjectMocks SubscriptionController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() { mvc = MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    void getSubscriptionByIdReturnsSubscription() throws Exception {
        Subscription subscription = new Subscription(12L, 7L, 1L, "Basic", BillingCycle.MONTHLY,
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-11-01"), new BigDecimal("9.99"), null);
        when(service.findById(12L)).thenReturn(subscription);

        mvc.perform(get("/api/subscription/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(12))
                .andExpect(jsonPath("$.data.planName").value("Basic"));

        verify(service).findById(12L);
    }
}
