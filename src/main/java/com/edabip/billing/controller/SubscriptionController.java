package com.edabip.billing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.edabip.billing.exceptionHandler.ApiResponse;
import com.edabip.billing.model.ChangePlanRequest;
import com.edabip.billing.model.CreateSubscriptionRequest;
import com.edabip.billing.model.Subscription;
import com.edabip.billing.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/subscription")
@Tag(name = "Subscriptions")
public class SubscriptionController {
    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Get the current subscription for a customer")
    public ApiResponse<Subscription> findCurrent(@RequestParam @NotNull @Positive Long customerId) {
        return ApiResponse.ok(service.findCurrentByCustomerId(customerId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a subscription", description = "Example: {\"customerId\":1,\"planId\":1,\"billingCycle\":\"MONTHLY\",\"currentPeriodStart\":\"2026-10-01\",\"currentPeriodEnd\":\"2026-11-01\"}")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Subscription created using the selected plan's current price",
            content = @Content(examples = @ExampleObject(value = """
                    {"success":true,"data":{"id":1,"customerId":1,"planId":1,"planName":"Basic","billingCycle":"MONTHLY","currentPeriodStart":"2026-10-01","currentPeriodEnd":"2026-11-01","amountDue":9.99,"lastPaymentAt":null},"error":null}
                    """)))
    public ApiResponse<Subscription> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a subscription by ID")
    public ApiResponse<Subscription> findById(@PathVariable @Positive long id) {
        return ApiResponse.ok(service.findById(id));
    }

    @PutMapping("/{id}/plan")
    @Operation(summary = "Change the plan for a subscription", description = "Example payload: {\"planId\":2}. The subscription amount due is updated to the selected plan's price.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription updated to the new plan",
            content = @Content(examples = @ExampleObject(value = """
                    {"success":true,"data":{"id":1,"customerId":1,"planId":2,"planName":"Standard","billingCycle":"MONTHLY","currentPeriodStart":"2026-10-01","currentPeriodEnd":"2026-11-01","amountDue":29.99,"lastPaymentAt":null},"error":null}
                    """)))
    public ApiResponse<Subscription> changePlan(@PathVariable @Positive long id,
                                                 @Valid @RequestBody ChangePlanRequest request) {
        return ApiResponse.ok(service.changePlan(id, request));
    }
}
