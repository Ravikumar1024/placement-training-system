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
    @NotBlank(message = "{student.code.required}")
    @Pattern(regexp = "^\\S+$", message = "{student.code.noSpaces}")
    @Size(max = 50, message = "{student.code.maxSize}")
    private String code;

    @Column(nullable = false)
    @NotBlank(message = "{student.name.required}")
    @Size(min = 2, max = 100, message = "{student.name.size}")
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "{student.email.required}")
    @Email(message = "{student.email.invalid}")
    private String email;

    @Column(length = 15)
    @Size(max = 15, message = "{student.phone.maxSize}")
    private String phone;
    
    @Column(length = 50)
    @Size(max = 50, message = "{student.department.maxSize}")
    private String department;
    
    @Column(length = 10)
    @Size(max = 10, message = "{student.batch.maxSize}")
    private String batch;
    
    @DecimalMin(value = "0.0", message = "{student.cgpa.min}")
    @DecimalMax(value = "10.0", message = "{student.cgpa.max}")
    private Double cgpa;
    
    @Min(value = 0, message = "{student.backlogs.min}")
    @Max(value = 10, message = "{student.backlogs.max}")
    private Integer backlogs;
    
    @Size(max = 500, message = "{student.skills.maxSize}")
    private String skills;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
