package com.edabip.billing.model;

public record AuthUser(long userId, String name, String password, String email, String role, String status) {}
