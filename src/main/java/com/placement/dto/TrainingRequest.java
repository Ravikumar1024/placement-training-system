package com.placement.dto;

import com.placement.entity.Training;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TrainingRequest(
    @NotBlank @Size(min = 2, max = 100) String trainingName,
    @Size(max = 500) String description,
    @Size(max = 100) String trainer,
    Training.DeliveryMode mode,
    @Size(max = 200) String location,
    @Size(max = 500) @Pattern(regexp = "^$|https?://.+") String meetingUrl,
    @Size(max = 500) @Pattern(regexp = "^$|https://(www\\.)?(youtube\\.com/(watch\\?v=|embed/)|youtu\\.be/)[A-Za-z0-9_-]{11}([&?].*)?$") String videoUrl,
    @NotNull LocalDate startDate,
    LocalDate endDate
) {}