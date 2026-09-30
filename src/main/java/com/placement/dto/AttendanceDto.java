package com.placement.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDto {
    private String id;
    private String studentName;
    private String trainingName;
    private LocalDate date;
    private boolean present;
}