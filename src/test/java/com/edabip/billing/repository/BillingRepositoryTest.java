package com.edabip.billing.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class BillingRepositoryTest {
    @Mock JdbcTemplate jdbc;
    @InjectMocks BillingRepository repository;

    @Test
    void countInvoicesFiltersByCustomerAndStatus() {
        when(jdbc.queryForObject(eq("SELECT COUNT(*) FROM invoices WHERE customer_id = ? AND status = ?"),
                eq(Long.class), eq(23L), eq("Due"))).thenReturn(2L);

        assertEquals(2L, repository.countInvoices(23L, "Due"));
        verify(jdbc).queryForObject("SELECT COUNT(*) FROM invoices WHERE customer_id = ? AND status = ?",
                Long.class, 23L, "Due");
    }
}
