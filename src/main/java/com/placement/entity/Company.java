package com.placement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Company {
    @Id 
    @GeneratedValue(generator = "UUID4")
    @org.hibernate.annotations.GenericGenerator(name = "UUID4", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(nullable = false)
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;

    @Size(max = 100, message = "Job role must be at most 100 characters")
    private String jobRole;
    
    @DecimalMin(value = "0.0", message = "Package cannot be negative")
    private Double packageLpa;
    
    @DecimalMin(value = "0.0", message = "Minimum CGPA cannot be negative")
    private Double minCgpa;
    
    @Min(value = 0, message = "Maximum backlogs cannot be negative")
    private Integer maxBacklogs;
    
    @DecimalMin(value = "0.0", message = "Minimum aptitude score cannot be negative")
    @DecimalMax(value = "100.0", message = "Minimum aptitude score cannot exceed 100")
    private Double minAptitudeScore;
    
    @DecimalMin(value = "0.0", message = "Minimum attendance cannot be negative")
    @DecimalMax(value = "100.0", message = "Minimum attendance cannot exceed 100")
    private Double minAttendance;
    
    @Size(max = 200, message = "Eligible departments must be at most 200 characters")
    private String eligibleDepartments;
    
    @Size(max = 500, message = "Required skills must be at most 500 characters")
    private String requiredSkills;
}
