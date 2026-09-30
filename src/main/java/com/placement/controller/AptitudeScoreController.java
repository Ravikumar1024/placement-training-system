package com.placement.controller;

import com.placement.dto.Result;
import com.placement.dto.AptitudeScoreRequest;
import com.placement.entity.AptitudeScore;
import com.placement.entity.Student;
import com.placement.entity.AptitudeTest;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/aptitude-scores")
@RequiredArgsConstructor
public class AptitudeScoreController {
    private final AptitudeScoreRepository repository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Result<List<AptitudeScore>>> all() {
        return ResponseEntity.ok(Result.success("Aptitude scores retrieved successfully", repository.findAll()));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Result<List<AptitudeScore>>> byStudent(@PathVariable("studentId") String studentId, Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
        if (user.getRole() != com.placement.entity.User.Role.STUDENT || user.getStudent() == null || !user.getStudent().getId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Students may only view their own score history.");
        }
        return ResponseEntity.ok(Result.success("Student aptitude scores retrieved successfully", repository.findByStudent_IdOrderByTest_TestDateDesc(studentId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<AptitudeScore>> one(@PathVariable("id") String id) {
        AptitudeScore score = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Aptitude score not found: " + id));
        return ResponseEntity.ok(Result.success("Aptitude score retrieved successfully", score));
    }

    @PostMapping
    public ResponseEntity<Result<AptitudeScore>> create(@Valid @RequestBody AptitudeScoreRequest request) {
        AptitudeScore value = toEntity(request);
        AptitudeScore saved = repository.save(value);
        return ResponseEntity.ok(Result.success("Aptitude score recorded successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<AptitudeScore>> update(@PathVariable("id") String id, @Valid @RequestBody AptitudeScoreRequest request) {
        AptitudeScore value = toEntity(request);
        value.setId(id);
        AptitudeScore saved = repository.save(value);
        return ResponseEntity.ok(Result.success("Aptitude score updated successfully", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Result.success("Aptitude score deleted successfully",null));
    }

    private AptitudeScore toEntity(AptitudeScoreRequest request) {
        return AptitudeScore.builder()
            .student(Student.builder().id(request.student().id()).build())
            .test(AptitudeTest.builder().id(request.test().id()).build())
            .score(request.score())
            .build();
    }
}
