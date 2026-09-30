package com.placement.dto;

public record AptitudeQuestionAdminView(
    String id,
    String prompt,
    String optionA,
    String optionB,
    String optionC,
    String optionD,
    String correctOption,
    Double marks
) {}