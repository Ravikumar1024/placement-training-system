package com.placement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AttendanceRequest(
    @NotNull @Valid Reference student,
    @NotNull @Valid Reference training,
    @NotNull LocalDate date,
    @NotNull Boolean present
) {
    public record Reference(@NotBlank String id) {}
}