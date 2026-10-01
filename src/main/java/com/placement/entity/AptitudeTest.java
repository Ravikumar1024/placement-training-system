package com.placement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "aptitude_tests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AptitudeTest {
    @Id @GeneratedValue(generator = "UUID4")
    @org.hibernate.annotations.GenericGenerator(name = "UUID4", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(nullable = false)
    @NotBlank(message = "{aptitudeTest.name.required}")
    @Size(min = 2, max = 100, message = "{aptitudeTest.name.size}")
    private String testName;

    private LocalDate testDate;
    
    @DecimalMin(value = "0.0", message = "{aptitudeTest.totalMarks.min}")
    private Double totalMarks;
}
