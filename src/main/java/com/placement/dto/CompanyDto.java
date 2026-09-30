package com.placement.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {
    private String id;
    private String companyName;
    private String jobRole;
    private Double packageLpa;
    private Double minCgpa;
    private Integer maxBacklogs;
    private Double minAptitudeScore;
    private Double minAttendance;
    private String eligibleDepartments;
    private String requiredSkills;
}