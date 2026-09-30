package com.placement.dto;

import java.util.List;

public record AptitudeAnswerReview(
    String prompt,
    List<String> options,
    String selectedOption,
    String correctOption,
    Double marks
) {}