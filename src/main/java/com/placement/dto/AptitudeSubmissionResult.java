package com.placement.dto;

public record AptitudeSubmissionResult(
    String scoreId,
    String testName,
    Double earnedMarks,
    Double totalMarks,
    Double score
) {}