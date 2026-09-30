package com.placement.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingDto {
    private String id;
    private String trainingName;
    private String description;
    private String trainer;
    private LocalDate startDate;
    private LocalDate endDate;
}