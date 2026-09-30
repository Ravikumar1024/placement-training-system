package com.placement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "trainings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Training {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(nullable = false)
    @NotBlank(message = "Training name is required")
    @Size(min = 2, max = 100, message = "Training name must be between 2 and 100 characters")
    private String trainingName;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
    
    @Size(max = 100, message = "Trainer name must be at most 100 characters")
    private String trainer;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_mode", nullable = false, length = 20)
    @NotNull(message = "Training delivery mode is required")
    @Builder.Default
    private DeliveryMode mode = DeliveryMode.ONSITE;

    @Size(max = 200, message = "Training location must be at most 200 characters")
    private String location;

    @Column(name = "meeting_url", length = 500)
    @Size(max = 500, message = "Meeting link must be at most 500 characters")
    @Pattern(regexp = "^$|https?://.+", message = "Meeting link must start with http:// or https://")
    private String meetingUrl;

    @Column(name = "video_url", length = 500)
    @Size(max = 500, message = "Training video link must be at most 500 characters")
    @Pattern(regexp = "^$|https://(www\\.)?(youtube\\.com/(watch\\?v=|embed/)|youtu\\.be/)[A-Za-z0-9_-]{11}([&?].*)?$", message = "Training video must be a YouTube video URL")
    private String videoUrl;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;

    public enum DeliveryMode { ONSITE, ONLINE }
}
