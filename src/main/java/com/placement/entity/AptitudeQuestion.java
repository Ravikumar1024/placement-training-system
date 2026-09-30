package com.placement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aptitude_questions", indexes = @Index(name = "idx_aptitude_question_test", columnList = "test_id"))
@Getter
@Setter
@NoArgsConstructor
public class AptitudeQuestion {
    @Id
    @GeneratedValue(generator = "UUID4")
    @org.hibernate.annotations.GenericGenerator(name = "UUID4", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AptitudeTest test;

    @Column(nullable = false, length = 1000)
    @NotBlank
    private String prompt;

    @Column(name = "option_a", nullable = false, length = 500)
    @NotBlank
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 500)
    @NotBlank
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 500)
    @NotBlank
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 500)
    @NotBlank
    private String optionD;

    @Column(nullable = false, length = 1)
    @NotBlank
    @Pattern(regexp = "[ABCD]")
    private String correctOption;

    @Column(nullable = false)
    @DecimalMin(value = "0.01")
    private Double marks;
}