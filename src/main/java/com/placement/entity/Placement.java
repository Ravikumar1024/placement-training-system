package com.placement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "placements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Placement {
    @Id
    @GeneratedValue(generator = "UUID4")
    @org.hibernate.annotations.GenericGenerator(name = "UUID4", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate appliedDate;
    private LocalDate placementDate;

    public enum Status {
        APPLIED, APTITUDE_CLEARED, TECHNICAL_ROUND, HR_ROUND, SELECTED, REJECTED
    }
}
