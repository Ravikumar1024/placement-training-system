package com.placement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AptitudeQuestionImportRequest(@NotEmpty List<@Valid AptitudeQuestionRequest> questions) {}