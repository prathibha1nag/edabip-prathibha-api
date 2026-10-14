package com.edabip.billing.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edabip.billing.exceptionHandler.ApiResponse;
import com.edabip.billing.model.Invoice;
import com.edabip.billing.model.PageResult;
import com.edabip.billing.model.UsageOverview;
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
}

