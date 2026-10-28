package com.edabip.billing.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.edabip.billing.model.InvoiceSummary;
import com.edabip.billing.service.BillingService;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {
    @Mock BillingService service;
    @InjectMocks BillingController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() { mvc = MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    void invoiceSummaryRequiresCustomerIdAndReturnsSummary() throws Exception {
        when(service.invoiceSummary(8L))
                .thenReturn(new InvoiceSummary(new BigDecimal("20.00"), 3, new BigDecimal("5.00")));

        mvc.perform(get("/api/invoices/summary").param("customerId", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPaid").value(20.0))
                .andExpect(jsonPath("$.data.totalInvoices").value(3));

        verify(service).invoiceSummary(8L);
    }
}
