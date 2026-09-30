package com.placement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AptitudeQuestionRequest(
    @NotBlank @Size(max = 1000) String prompt,
    @NotBlank @Size(max = 500) String optionA,
    @NotBlank @Size(max = 500) String optionB,
    @NotBlank @Size(max = 500) String optionC,
    @NotBlank @Size(max = 500) String optionD,
    @NotBlank @Pattern(regexp = "[ABCD]") String correctOption,
    @NotNull @DecimalMin(value = "0.01") Double marks
) {}