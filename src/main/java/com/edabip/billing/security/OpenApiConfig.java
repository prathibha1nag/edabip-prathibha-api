package com.edabip.billing.security;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
@SecurityScheme(name = OpenApiConfig.BEARER_SCHEME, type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
    static final String BEARER_SCHEME = "BearerAuth";
}
