package com.placement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record BulkAttendanceRequest(
    @NotBlank String trainingId,
    @NotNull LocalDate date,
    @NotEmpty List<@Valid Entry> entries
) {
    public record Entry(@NotBlank String studentId, @NotNull Boolean present) {}
}