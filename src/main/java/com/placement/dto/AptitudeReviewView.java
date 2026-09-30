package com.placement.dto;

import java.util.List;

public record AptitudeReviewView(
    String testName,
    Double earnedMarks,
    Double totalMarks,
    Double score,
    List<AptitudeAnswerReview> answers
) {}