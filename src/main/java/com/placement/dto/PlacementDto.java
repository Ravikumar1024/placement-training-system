package com.placement.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDto {
    private String id;
    private String studentName;
    private String companyName;
    private String jobRole;
    private Double packageLpa;
    private String status;
    private LocalDate appliedDate;
    private LocalDate placementDate;
}