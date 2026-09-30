package com.placement.dto;

import com.placement.entity.Placement;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PlacementRequest(
    @NotNull String studentId,
    @NotNull String companyId,
    Placement.Status status,
    LocalDate appliedDate,
    LocalDate placementDate
) {}
