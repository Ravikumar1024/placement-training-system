package com.placement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "students")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Student {
    @Id 
    @GeneratedValue(generator = "UUID4")
    @org.hibernate.annotations.GenericGenerator(name = "UUID4", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Student code is required")
    @Pattern(regexp = "^\\S+$", message = "Student code must not contain spaces")
    @Size(max = 50, message = "Student code must be at most 50 characters")
    private String code;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(length = 15)
    @Size(max = 15, message = "Phone number must be at most 15 characters")
    private String phone;
    
    @Column(length = 50)
    @Size(max = 50, message = "Department name must be at most 50 characters")
    private String department;
    
    @Column(length = 10)
    @Size(max = 10, message = "Batch must be at most 10 characters")
    private String batch;
    
    @DecimalMin(value = "0.0", message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10.0")
    private Double cgpa;
    
    @Min(value = 0, message = "Backlogs cannot be negative")
    @Max(value = 10, message = "Backlogs cannot exceed 10")
    private Integer backlogs;
    
    @Size(max = 500, message = "Skills must be at most 500 characters")
    private String skills;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
