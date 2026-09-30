package com.placement.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AptitudeScoreDto {
    private String id;
    private String studentName;
    private String testName;
    private Double score;
}