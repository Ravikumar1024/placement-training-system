package com.placement.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String batch;
    private Double cgpa;
    private Integer backlogs;
    private String skills;
}