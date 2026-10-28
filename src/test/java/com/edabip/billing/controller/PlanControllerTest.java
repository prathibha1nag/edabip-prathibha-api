package com.edabip.billing.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.edabip.billing.model.PageResult;
import com.edabip.billing.model.Plan;
import com.edabip.billing.service.PlanService;

@ExtendWith(MockitoExtension.class)
class PlanControllerTest {
    @Mock PlanService service;
    @InjectMocks PlanController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() { mvc = MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    void listPlansBindsPagingAndSortingParameters() throws Exception {
        Plan plan = new Plan(1L, "Basic", new BigDecimal("9.99"), 5, 10, 100, 3, 20, "Email", true);
        when(service.findAll(0, 20, "price,asc"))
                .thenReturn(new PageResult<>(List.of(plan), 0, 20, 1, 1, "price,asc"));

        mvc.perform(get("/api/plans").param("page", "0").param("size", "20").param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("Basic"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(service).findAll(0, 20, "price,asc");
    }
}
