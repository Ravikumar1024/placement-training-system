package com.placement.controller;

import com.placement.dto.Result;
import com.placement.dto.TrainingRequest;
import com.placement.entity.Attendance;
import com.placement.entity.Training;
import com.placement.repository.UserRepository;
import com.placement.service.AttendanceService;
import com.placement.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;
import java.time.Clock;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {
    private final TrainingService service;
    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final Clock clock;

    @GetMapping
    public ResponseEntity<Result<List<Training>>> all() {
        return ResponseEntity.ok(Result.success("Trainings retrieved successfully", service.findAll()));
    }

    @GetMapping("/current")
    public ResponseEntity<Result<List<Training>>> current() {
        LocalDate today = LocalDate.now(clock);
        List<Training> currentTrainings = service.findAll().stream()
            .filter(training -> !training.getStartDate().isAfter(today))
            .filter(training -> training.getEndDate() == null || !training.getEndDate().isBefore(today))
            .toList();
        return ResponseEntity.ok(Result.success("Current trainings retrieved successfully", currentTrainings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Training>> one(@PathVariable("id") String id) {
        Training training = service.findById(id);
        return ResponseEntity.ok(Result.success("Training retrieved successfully", training));
    }

    @PostMapping("/{id}/video-completion")
    public ResponseEntity<Result<Attendance>> videoCompleted(@PathVariable("id") String id, Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
        if (user.getRole() != com.placement.entity.User.Role.STUDENT || user.getStudent() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A linked student account is required to record video attendance.");
        }
        Training training = service.findById(id);
        if (training.getVideoUrl() == null || training.getVideoUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This training does not have an assigned video.");
        }
        Attendance attendance = attendanceService.recordVideoCompletion(user.getStudent().getId(), id, LocalDate.now(clock));
        return ResponseEntity.ok(Result.success("Video completion recorded as present for today", attendance));
    }

    @PostMapping
    public ResponseEntity<Result<Training>> create(@Valid @RequestBody TrainingRequest request) {
        Training value = toEntity(request);
        Training saved = service.save(value);
        return ResponseEntity.ok(Result.success("Training created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Training>> update(@PathVariable("id") String id, @Valid @RequestBody TrainingRequest request) {
        Training value = toEntity(request);
        value.setId(id);
        Training saved = service.save(value);
        return ResponseEntity.ok(Result.success("Training updated successfully", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok(Result.success("Training deleted successfully",null));
    }

    private Training toEntity(TrainingRequest request) {
        return Training.builder()
            .trainingName(request.trainingName())
            .description(request.description())
            .trainer(request.trainer())
            .mode(request.mode() == null ? Training.DeliveryMode.ONSITE : request.mode())
            .location(request.location())
            .meetingUrl(request.meetingUrl())
            .videoUrl(request.videoUrl())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .build();
    }
}
