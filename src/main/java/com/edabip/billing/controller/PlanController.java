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
import com.edabip.billing.model.CreatePlanRequest;
import com.edabip.billing.model.Plan;
import com.edabip.billing.model.PageResult;
import com.edabip.billing.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
    @Operation(summary = "List plans", description = "Uses zero-based pagination. Example: /api/plans?page=0&size=20&sort=price,asc")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "A page of plans",
            content = @Content(examples = @ExampleObject(value = """
                    {"success":true,"data":{"items":[{"id":1,"name":"Basic","price":9.99,"userLimit":5,"storageLimitGb":10,"reportsPerMonth":100,"activeDashboardLimit":3,"dataProcessingLimitGb":20,"support":"Email","active":true}],"page":0,"size":20,"totalElements":3,"totalPages":1,"sort":"price,asc"},"error":null}
                    """)))
    public ApiResponse<PageResult<Plan>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(example = "price,asc") @RequestParam(defaultValue = "price,asc") String sort) {
        return ApiResponse.ok(service.findAll(page, size, sort));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a plan", description = "Example: {\"name\":\"Business\",\"price\":49.99,\"userLimit\":25,\"storageLimitGb\":100,\"reportsPerMonth\":500,\"activeDashboardLimit\":10,\"dataProcessingLimitGb\":100,\"support\":\"Priority email\"}")
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
