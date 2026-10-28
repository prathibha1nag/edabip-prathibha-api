package com.edabip.billing.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edabip.billing.entity.PlanEntity;
import com.edabip.billing.entity.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class SubscriptionRepositoryTest {
    @Mock SubscriptionJpaRepository jpa;
    @Mock PlanJpaRepository plans;
    @InjectMocks SubscriptionRepository repository;

    @Test
    void findByIdMapsSubscriptionAndRelatedPlan() {
        PlanEntity plan = org.mockito.Mockito.mock(PlanEntity.class);
        when(plan.getId()).thenReturn(3L);
        when(plan.getName()).thenReturn("Standard");

        SubscriptionEntity entity = org.mockito.Mockito.mock(SubscriptionEntity.class);
        when(entity.getId()).thenReturn(9L);
        when(entity.getCustomerId()).thenReturn(12L);
        when(entity.getPlan()).thenReturn(plan);
        when(entity.getBillingCycle()).thenReturn("monthly");
        when(entity.getCurrentPeriodStart()).thenReturn(LocalDate.parse("2026-10-01"));
        when(entity.getCurrentPeriodEnd()).thenReturn(LocalDate.parse("2026-11-01"));
        when(entity.getAmountDue()).thenReturn(new BigDecimal("29.99"));
        when(entity.getLastPaymentAt()).thenReturn(null);
        when(jpa.findDetailedById(9L)).thenReturn(Optional.of(entity));

        var result = repository.findById(9L);

        assertTrue(result.isPresent());
        assertEquals("Standard", result.get().planName());
        assertEquals(12L, result.get().customerId());
        assertEquals(new BigDecimal("29.99"), result.get().amountDue());
    }
}
