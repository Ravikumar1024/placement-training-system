package com.placement.dto;

import com.placement.entity.Placement;
import java.time.LocalDate;

public record PlacementStatusResponse(
	String placementId,
	String studentId,
    String companyName,
    String jobRole,
    Double packageLpa,
    Placement.Status status,
    LocalDate appliedDate,
    LocalDate placementDate
) {}
