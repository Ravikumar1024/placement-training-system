package com.placement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {
    @Id @GeneratedValue(generator = "UUID4")
    @org.hibernate.annotations.GenericGenerator(name = "UUID4", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @ManyToOne(optional = false)
    @NotNull(message = "{attendance.student.required}")
    private Student student;

    @ManyToOne(optional = false)
    @NotNull(message = "{attendance.training.required}")
    private Training training;

    @Column(nullable = false)
    @NotNull(message = "{attendance.date.required}")
    private LocalDate date;

    @Column(nullable = false)
    @NotNull(message = "{attendance.present.required}")
    private boolean present;
    
    public boolean isPresent() {
        return present;
    }
}
