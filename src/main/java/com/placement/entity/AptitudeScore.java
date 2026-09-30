package com.placement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aptitude_scores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AptitudeScore {
    @Id @jakarta.persistence.GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @ManyToOne(optional = false)
    @NotNull(message = "Student is required")
    private Student student;

    @ManyToOne(optional = false)
    @NotNull(message = "Aptitude test is required")
    private AptitudeTest test;

    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    @DecimalMax(value = "100.0", message = "Score cannot exceed 100")
    private Double score;

    @Column(name = "completed_attempt", nullable = false)
    @Builder.Default
    private boolean completedAttempt = false;

    @Column(name = "attempt_earned_marks")
    private Double attemptEarnedMarks;

    @Column(name = "attempt_total_marks")
    private Double attemptTotalMarks;
}
