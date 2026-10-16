package com.edabip.billing.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.CreatePaymentMethodRequest;
import com.edabip.billing.model.Invoice;
import com.edabip.billing.model.PaymentMethod;
import com.edabip.billing.repository.BillingRepository;
import com.edabip.billing.repository.BillingRepository.UsageSnapshot;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {
    @Mock BillingRepository billing;
    @InjectMocks BillingService service;

    private UsageSnapshot usage;

    @BeforeEach
    void setUp() {
        usage = new UsageSnapshot("Basic", bd("4"), bd("6.25"), bd("37"), bd("2"), bd("8.50"),
                bd("5"), bd("10"), bd("100"), bd("3"), bd("20"));
    }

    @Test
    void usageReturnsAllFiveMetricsAndCalculatesPercentages() {
        when(billing.findUsage(1)).thenReturn(Optional.of(usage));
        var result = service.getUsage(1);
        assertEquals(5, result.metrics().size());
        assertEquals("Basic", result.planName());
        assertEquals(bd("80.00"), result.metrics().get(0).percentageUsed());
        assertEquals(bd("62.50"), result.metrics().get(1).percentageUsed());
    }

    @Test
    void usageNotFoundThrowsNotFound() {
        when(billing.findUsage(404)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getUsage(404));
    }

    @Test
    void usageWithZeroLimitAvoidsDivisionByZero() {
        var zeroLimit = new UsageSnapshot("Free", bd("1"), bd("0"), bd("0"), bd("0"), bd("0"),
                bd("0"), bd("0"), bd("0"), bd("0"), bd("0"));
        when(billing.findUsage(1)).thenReturn(Optional.of(zeroLimit));
        assertEquals(bd("100"), service.getUsage(1).metrics().get(0).percentageUsed());
        assertEquals(bd("0"), service.getUsage(1).metrics().get(1).percentageUsed());
    }

    @Test
    void invoiceListNormalizesFilterAndReturnsPageMetadata() {
        Invoice invoice = new Invoice(8L, java.time.LocalDate.parse("2026-10-01"), java.time.LocalDate.parse("2026-11-01"), bd("9.99"), "Due", null);
        when(billing.countInvoices(1, "Due")).thenReturn(21L);
        when(billing.findInvoices(1, "Due", "amount", "ASC", 10, 10L)).thenReturn(List.of(invoice));
        var result = service.listInvoices(1, 1, 10, " due ", "amount,asc");
        assertEquals(3, result.totalPages());
        assertEquals(21, result.totalElements());
        assertEquals(8L, result.items().get(0).id());
    }

    @Test
    void invoiceListRejectsNegativePage() {
        assertThrows(IllegalArgumentException.class, () -> service.listInvoices(1, -1, 10, null, "periodStart,desc"));
        verifyNoInteractions(billing);
    }

    @Test
    void invoiceListRejectsInvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> service.listInvoices(1, 0, 10, "overdue", "periodStart,desc"));
        verifyNoInteractions(billing);
    }

    @Test
    void invoiceListRejectsUnsupportedSortField() {
        assertThrows(IllegalArgumentException.class, () -> service.listInvoices(1, 0, 10, null, "customerId,asc"));
        verifyNoInteractions(billing);
    }

    @Test
    void setPrimaryClearsOldMethodAndReturnsNewPrimary() {
        var old = new PaymentMethod(1L, 1, "Visa", "1111", 12, 2028, true);
        var next = new PaymentMethod(2L, 1, "Mastercard", "2222", 12, 2028, true);
        when(billing.findPaymentMethod(1, 2)).thenReturn(List.of(next));
        when(billing.paymentMethods(1)).thenReturn(List.of(old));
        when(billing.findPaymentMethod(1, 2)).thenReturn(List.of(next));
        assertEquals(2L, service.setPrimaryPaymentMethod(1, 2).id());
        verify(billing).setPaymentMethodPrimary(1, 1, false);
        verify(billing).setPaymentMethodPrimary(1, 2, true);
    }

    @Test
    void setPrimaryRejectsMissingMethod() {
        when(billing.findPaymentMethod(1, 99)).thenReturn(List.of());
        assertThrows(NotFoundException.class, () -> service.setPrimaryPaymentMethod(1, 99));
        verify(billing, never()).setPaymentMethodPrimary(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void removeRejectsPrimaryMethod() {
        when(billing.findPaymentMethod(1, 1)).thenReturn(List.of(new PaymentMethod(1L, 1, "Visa", "1111", 12, 2028, true)));
        assertThrows(IllegalArgumentException.class, () -> service.removePaymentMethod(1, 1));
        verify(billing, never()).deletePaymentMethod(anyLong(), anyLong());
    }

    @Test
    void addPaymentMethodRejectsExpiredCard() {
        var expired = new CreatePaymentMethodRequest("Visa", "4242", 1, 2000, true);
        assertThrows(IllegalArgumentException.class, () -> service.addPaymentMethod(1, expired));
        verifyNoInteractions(billing);
    }

    @Test
    void firstPaymentMethodBecomesPrimary() {
        int year = java.time.Year.now().getValue() + 2;
        var request = new CreatePaymentMethodRequest("Visa", "4242", 12, year, false);
        var saved = new PaymentMethod(5L, 1, "Visa", "4242", 12, year, true);
        when(billing.paymentMethods(1)).thenReturn(List.of(), List.of(saved));
        when(billing.addPaymentMethod(1, "Visa", "4242", 12, year, true)).thenReturn(1);
        assertEquals(5L, service.addPaymentMethod(1, request).id());
    }

    @Test
    void addPaymentMethodPropagatesDuplicateConstraintFailure() {
        var request = new CreatePaymentMethodRequest("Visa", "4242", 12, java.time.Year.now().getValue() + 2, false);
        when(billing.paymentMethods(1)).thenReturn(List.of(new PaymentMethod(2L, 1, "Mastercard", "4444", 12, 2029, true)));
        when(billing.addPaymentMethod(1, "Visa", "4242", 12, request.expiryYear(), false))
                .thenThrow(new DataIntegrityViolationException("duplicate payment method"));
        assertThrows(DataIntegrityViolationException.class, () -> service.addPaymentMethod(1, request));
    }

    private static BigDecimal bd(String value) { return new BigDecimal(value); }
}
