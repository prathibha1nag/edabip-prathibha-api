package com.edabip.billing.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.PageResult;
import com.edabip.billing.model.Plan;
import com.edabip.billing.repository.PlanRepository;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {
    @Mock PlanRepository repository;
    @InjectMocks PlanService service;

    @Test
    void listPlansReturnsRequestedPage() {
        var expected = new PageResult<>(List.of(plan(true)), 0, 10, 1, 1, "price,asc");
        when(repository.findAll(0, 10, "price", "ASC", "price,asc")).thenReturn(expected);
        assertEquals(expected, service.findAll(0, 10, "price,asc"));
    }

    @Test
    void listPlansRejectsInvalidPageSize() {
        assertThrows(IllegalArgumentException.class, () -> service.findAll(-1, 10, "price,asc"));
        assertThrows(IllegalArgumentException.class, () -> service.findAll(0, 101, "price,asc"));
        verifyNoInteractions(repository);
    }

    @Test
    void listPlansRejectsUnknownSortField() {
        assertThrows(IllegalArgumentException.class, () -> service.findAll(0, 10, "id,asc"));
        verifyNoInteractions(repository);
    }

    @Test
    void activateUpdatesAndReturnsPlan() {
        var inactive = plan(false);
        var active = plan(true);
        when(repository.findById(1)).thenReturn(Optional.of(inactive), Optional.of(active));
        Plan result = service.activate(1);
        assertTrue(result.active());
        verify(repository).setActive(1, true);
    }

    @Test
    void activateMissingPlanThrowsNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.activate(99));
        verify(repository, never()).setActive(anyLong(), anyBoolean());
    }

    private Plan plan(boolean active) {
        return new Plan(1L, "Basic", new BigDecimal("9.99"), 5, 10, 100, 3, 20, "Email", active);
    }
}
