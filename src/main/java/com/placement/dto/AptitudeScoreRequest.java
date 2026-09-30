package com.placement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AptitudeScoreRequest(
    @NotNull @Valid Reference student,
    @NotNull @Valid Reference test,
    @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double score
) {
    public record Reference(@jakarta.validation.constraints.NotBlank String id) {}
}