package com.placement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record AptitudeSubmissionRequest(
    @NotBlank String studentId,
    @NotEmpty List<@Valid Answer> answers
) {
    public record Answer(
        @NotBlank String questionId,
        @NotBlank @Pattern(regexp = "[ABCD]") String selectedOption
    ) {}
}