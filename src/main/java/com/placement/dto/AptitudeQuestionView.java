package com.placement.dto;

public record AptitudeQuestionView(
    String id,
    String prompt,
    String optionA,
    String optionB,
    String optionC,
    String optionD,
    Double marks
) {}