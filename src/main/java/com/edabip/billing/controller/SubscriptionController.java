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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping({"/api/subscription", "/api/v1/subscriptions"})
@Tag(name = "Subscriptions")
public class SubscriptionController {
    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Get the current subscription for a customer")
    public ApiResponse<Subscription> findCurrent(@RequestParam @NotBlank String customerId) {
        return ApiResponse.ok(service.findCurrentByCustomerId(customerId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a subscription")
    public ApiResponse<Subscription> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a subscription by ID")
    public ApiResponse<Subscription> findById(@PathVariable @Positive long id) {
        return ApiResponse.ok(service.findById(id));
    }

    @PutMapping("/{id}/plan")
    @Operation(summary = "Change the plan for a subscription")
    public ApiResponse<Subscription> changePlan(@PathVariable @Positive long id,
                                                 @Valid @RequestBody ChangePlanRequest request) {
        return ApiResponse.ok(service.changePlan(id, request));
    }
}
