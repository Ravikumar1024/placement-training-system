package com.placement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AptitudeTestRequest(
    @jakarta.validation.constraints.NotBlank @Size(min = 2, max = 100) String testName,
    LocalDate testDate,
    @DecimalMin(value = "0.0") Double totalMarks
) {}