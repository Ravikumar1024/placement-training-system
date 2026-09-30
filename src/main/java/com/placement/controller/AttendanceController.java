package com.placement.controller;

import com.placement.dto.Result;
import com.placement.dto.BulkAttendanceRequest;
import com.placement.dto.AttendanceRequest;
import com.placement.entity.Attendance;
import com.placement.entity.Student;
import com.placement.entity.Training;
import com.placement.repository.UserRepository;
import com.placement.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService service;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Result<List<Attendance>>> all() {
        return ResponseEntity.ok(Result.success("Attendance records retrieved successfully", service.findAll()));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Result<List<Attendance>>> byStudent(@PathVariable("studentId") String studentId, Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
        if (user.getRole() != com.placement.entity.User.Role.STUDENT || user.getStudent() == null || !user.getStudent().getId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Students may only view their own attendance.");
        }
        List<Attendance> records = service.findAll().stream()
            .filter(attendance -> attendance.getStudent().getId().equals(studentId))
            .toList();
        return ResponseEntity.ok(Result.success("Student attendance retrieved successfully", records));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Attendance>> one(@PathVariable("id") String id) {
        Attendance attendance = service.findById(id);
        return ResponseEntity.ok(Result.success("Attendance record retrieved successfully", attendance));
    }

    @PostMapping
    public ResponseEntity<Result<Attendance>> create(@Valid @RequestBody AttendanceRequest request) {
        Attendance value = toEntity(request);
        Attendance saved = service.save(value);
        return ResponseEntity.ok(Result.success("Attendance record created successfully", saved));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Result<List<Attendance>>> bulk(@Valid @RequestBody BulkAttendanceRequest request) {
        List<Attendance> saved = service.saveForTrainingDate(request.trainingId(), request.date(), request.entries());
        return ResponseEntity.ok(Result.success("Attendance saved successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Attendance>> update(@PathVariable("id") String id, @Valid @RequestBody AttendanceRequest request) {
        Attendance value = toEntity(request);
        value.setId(id);
        Attendance saved = service.save(value);
        return ResponseEntity.ok(Result.success("Attendance record updated successfully", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok(Result.success("Attendance record deleted successfully",null));
    }

    private Attendance toEntity(AttendanceRequest request) {
        return Attendance.builder()
            .student(Student.builder().id(request.student().id()).build())
            .training(Training.builder().id(request.training().id()).build())
            .date(request.date())
            .present(request.present())
            .build();
    }
}
