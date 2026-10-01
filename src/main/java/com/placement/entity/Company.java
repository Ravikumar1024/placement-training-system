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
    @NotBlank(message = "{company.name.required}")
    @Size(min = 2, max = 100, message = "{company.name.size}")
    private String companyName;

    @Size(max = 100, message = "{company.jobRole.maxSize}")
    private String jobRole;
    
    @DecimalMin(value = "0.0", message = "{company.package.min}")
    private Double packageLpa;
    
    @DecimalMin(value = "0.0", message = "{company.minCgpa.min}")
    private Double minCgpa;
    
    @Min(value = 0, message = "{company.maxBacklogs.min}")
    private Integer maxBacklogs;
    
    @DecimalMin(value = "0.0", message = "{company.minAptitudeScore.min}")
    @DecimalMax(value = "100.0", message = "{company.minAptitudeScore.max}")
    private Double minAptitudeScore;
    
    @DecimalMin(value = "0.0", message = "{company.minAttendance.min}")
    @DecimalMax(value = "100.0", message = "{company.minAttendance.max}")
    private Double minAttendance;
    
    @Size(max = 200, message = "{company.departments.maxSize}")
    private String eligibleDepartments;
    
    @Size(max = 500, message = "{company.skills.maxSize}")
    private String requiredSkills;
}
