package com.edabip.billing.model;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}
