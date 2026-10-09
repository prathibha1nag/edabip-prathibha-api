package com.edabip.billing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.edabip.billing.exceptionHandler.ApiResponse;
import com.edabip.billing.model.CreatePlanRequest;
import com.edabip.billing.model.Plan;
import com.edabip.billing.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/plans")
@Tag(name = "Plans")
public class PlanController {
    private final PlanService service;

    public PlanController(PlanService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "List available plans")
    public ApiResponse<List<Plan>> findAll() { return ApiResponse.ok(service.findAll()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a plan")
    public ApiResponse<Plan> create(@Valid @RequestBody CreatePlanRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a plan by ID")
    public ApiResponse<Plan> findById(@PathVariable @Positive long id) {
        return ApiResponse.ok(service.findById(id));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate a plan")
    public ApiResponse<Plan> activate(@PathVariable @Positive long id) {
        return ApiResponse.ok(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a plan")
    public ApiResponse<Plan> deactivate(@PathVariable @Positive long id) {
        return ApiResponse.ok(service.deactivate(id));
    }
}
