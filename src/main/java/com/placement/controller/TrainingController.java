package com.placement.controller;

import com.placement.dto.Result;
import com.placement.dto.TrainingRequest;
import com.placement.entity.Attendance;
import com.placement.entity.Training;
import com.placement.repository.UserRepository;
import com.placement.service.AttendanceService;
import com.placement.service.TrainingService;
import com.placement.util.ApiMessages;
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
        return ResponseEntity.ok(Result.success("api.success.read.trainings.list", service.findAll()));
    }

    @GetMapping("/current")
    public ResponseEntity<Result<List<Training>>> current() {
        LocalDate today = LocalDate.now(clock);
        List<Training> currentTrainings = service.findAll().stream()
            .filter(training -> !training.getStartDate().isAfter(today))
            .filter(training -> training.getEndDate() == null || !training.getEndDate().isBefore(today))
            .toList();
        return ResponseEntity.ok(Result.success("api.success.read.trainings.current", currentTrainings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Training>> one(@PathVariable("id") String id) {
        Training training = service.findById(id);
        return ResponseEntity.ok(Result.success("api.success.read.trainings.one", training));
    }

    @PostMapping("/{id}/video-completion")
    public ResponseEntity<Result<Attendance>> videoCompleted(@PathVariable("id") String id, Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, ApiMessages.get("api.error.authentication.userNotFound")));
        if (user.getRole() != com.placement.entity.User.Role.STUDENT || user.getStudent() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ApiMessages.get("api.error.authentication.videoAttendanceStudentRequired"));
        }
        Training training = service.findById(id);
        if (training.getVideoUrl() == null || training.getVideoUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ApiMessages.get("api.error.video.notAssigned"));
        }
        Attendance attendance = attendanceService.recordVideoCompletion(user.getStudent().getId(), id, LocalDate.now(clock));
        return ResponseEntity.ok(Result.success("api.success.write.training.videoCompletion", attendance));
    }

    @PostMapping
    public ResponseEntity<Result<Training>> create(@Valid @RequestBody TrainingRequest request) {
        Training value = toEntity(request);
        Training saved = service.save(value);
        return ResponseEntity.ok(Result.success("api.success.write.training.created", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Training>> update(@PathVariable("id") String id, @Valid @RequestBody TrainingRequest request) {
        Training value = toEntity(request);
        value.setId(id);
        Training saved = service.save(value);
        return ResponseEntity.ok(Result.success("api.success.write.training.updated", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok(Result.success("api.success.write.training.deleted", null));
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
