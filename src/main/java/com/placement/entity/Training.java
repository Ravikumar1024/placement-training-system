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
    @NotBlank(message = "{training.name.required}")
    @Size(min = 2, max = 100, message = "{training.name.size}")
    private String trainingName;

    @Size(max = 500, message = "{training.description.maxSize}")
    private String description;
    
    @Size(max = 100, message = "{training.trainer.maxSize}")
    private String trainer;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_mode", nullable = false, length = 20)
    @NotNull(message = "{training.mode.required}")
    @Builder.Default
    private DeliveryMode mode = DeliveryMode.ONSITE;

    @Size(max = 200, message = "{training.location.maxSize}")
    private String location;

    @Column(name = "meeting_url", length = 500)
    @Size(max = 500, message = "{training.meetingUrl.maxSize}")
    @Pattern(regexp = "^$|https?://.+", message = "{training.meetingUrl.format}")
    private String meetingUrl;

    @Column(name = "video_url", length = 500)
    @Size(max = 500, message = "{training.videoUrl.maxSize}")
    @Pattern(regexp = "^$|https://(www\\.)?(youtube\\.com/(watch\\?v=|embed/)|youtu\\.be/)[A-Za-z0-9_-]{11}([&?].*)?$", message = "{training.videoUrl.format}")
    private String videoUrl;
    
    @NotNull(message = "{training.startDate.required}")
    private LocalDate startDate;
    
    private LocalDate endDate;

    public enum DeliveryMode { ONSITE, ONLINE }
}
