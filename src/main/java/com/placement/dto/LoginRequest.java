package com.placement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password,
    @Pattern(regexp = "ADMIN|STUDENT", message = "{login.expectedRole.invalid}") String expectedRole
) {}
