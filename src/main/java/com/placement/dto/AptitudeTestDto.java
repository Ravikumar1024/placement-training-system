package com.placement.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AptitudeTestDto {
    private String id;
    private String testName;
    private LocalDate testDate;
    private Double totalMarks;
}