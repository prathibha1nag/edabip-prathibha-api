package com.edabip.billing.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.edabip.billing.exceptionHandler.ApiResponse;
import com.edabip.billing.model.Invoice;
import com.edabip.billing.model.PageResult;
import com.edabip.billing.model.UsageOverview;
import com.edabip.billing.model.InvoiceSummary;
import com.edabip.billing.model.MonthlyInvoiceTotal;
import com.edabip.billing.model.PaymentMethod;
import com.edabip.billing.model.CreatePaymentMethodRequest;
import java.util.List;
import com.edabip.billing.service.BillingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api")
@Tag(name = "Billing")
public class BillingController {
    private final BillingService service;

    public BillingController(BillingService service) { this.service = service; }

    @GetMapping("/usage")
    @Operation(summary = "Get current usage against the customer's active plan",
            description = "Example: /api/usage?customerId=1. Returns used amount, plan limit and percentage for five usage metrics.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usage for the current plan",
            content = @Content(examples = @ExampleObject(value = """
                    {"success":true,"data":{"customerId":1,"planName":"Basic","metrics":[{"metric":"Active Users","used":4,"limit":5,"percentageUsed":80.00}]},"error":null}
                    """)))
    public ApiResponse<UsageOverview> getUsage(@RequestParam @Positive long customerId) {
        return ApiResponse.ok(service.getUsage(customerId));
    }

    @GetMapping("/invoices")
    @Operation(summary = "List invoices for a customer",
            description = "Example: /api/invoices?customerId=1&page=0&size=20&status=Due&sort=periodStart,desc. Page is zero-based; size is 1 to 100.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "A page of invoices",
            content = @Content(examples = @ExampleObject(value = """
                    {"success":true,"data":{"items":[{"id":6,"periodStart":"2026-10-01","periodEnd":"2026-11-01","amount":9.99,"status":"Due","paidOn":null}],"page":0,"size":20,"totalElements":1,"totalPages":1,"sort":"periodStart,desc"},"error":null}
                    """)))
    public ApiResponse<PageResult<Invoice>> listInvoices(
            @RequestParam @Positive long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @Parameter(example = "periodStart,desc") @RequestParam(defaultValue = "periodStart,desc") String sort) {
        return ApiResponse.ok(service.listInvoices(customerId, page, size, status, sort));
    }

    @GetMapping("/invoices/summary")
    @Operation(summary="Invoice totals for a customer", description="Example: GET /api/invoices/summary?customerId=1")
    public ApiResponse<InvoiceSummary> invoiceSummary(@RequestParam @Positive long customerId) {
        return ApiResponse.ok(service.invoiceSummary(customerId));
    }

    @GetMapping("/invoices/monthly-totals")
    @Operation(summary="Monthly invoice totals for the last six months", description="Example: GET /api/invoices/monthly-totals?customerId=1. Includes only months with invoices.")
    public ApiResponse<List<MonthlyInvoiceTotal>> monthlyInvoiceTotals(@RequestParam @Positive long customerId) {
        return ApiResponse.ok(service.monthlyInvoiceTotals(customerId));
    }

    @GetMapping("/payment-methods")
    @Operation(summary="List a customer's payment methods")
    public ApiResponse<List<PaymentMethod>> paymentMethods(@RequestParam @Positive long customerId) {
        return ApiResponse.ok(service.listPaymentMethods(customerId));
    }

    @PostMapping("/payment-methods")
    @Operation(summary="Add a payment method", description="Example payload: {\"customerId\":1,\"brand\":\"Visa\",\"lastFour\":\"4242\",\"expiryMonth\":12,\"expiryYear\":2028,\"makePrimary\":false}. Only brand, last four digits, and expiry are accepted; full PAN and CVV are never stored.")
    public ApiResponse<PaymentMethod> addPaymentMethod(@RequestParam @Positive long customerId,
            @jakarta.validation.Valid @RequestBody CreatePaymentMethodRequest request) {
        return ApiResponse.ok(service.addPaymentMethod(customerId, request));
    }

    @PutMapping("/payment-methods/{id}/primary")
    @Operation(summary="Set a payment method as primary", description="Runs the primary-method swap transactionally.")
    public ApiResponse<PaymentMethod> setPrimary(@RequestParam @Positive long customerId, @PathVariable @Positive long id) {
        return ApiResponse.ok(service.setPrimaryPaymentMethod(customerId,id));
    }

    @DeleteMapping("/payment-methods/{id}")
    @Operation(summary="Remove a non-primary payment method", description="A primary method must first be replaced.")
    public ApiResponse<Void> removePaymentMethod(@RequestParam @Positive long customerId, @PathVariable @Positive long id) {
        service.removePaymentMethod(customerId,id);
        return ApiResponse.ok(null);
    }
}

