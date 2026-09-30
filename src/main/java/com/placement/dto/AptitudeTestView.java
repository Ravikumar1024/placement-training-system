package com.placement.dto;

import java.time.LocalDate;

public record AptitudeTestView(String id, String testName, LocalDate testDate, Double totalMarks, long questionCount) {}