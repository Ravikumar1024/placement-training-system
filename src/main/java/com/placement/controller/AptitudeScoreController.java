package com.placement.controller;

import com.placement.dto.Result;
import com.placement.dto.AptitudeScoreRequest;
import com.placement.entity.AptitudeScore;
import com.placement.entity.Student;
import com.placement.entity.AptitudeTest;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.UserRepository;
import com.placement.util.ApiMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/aptitude-scores")
@RequiredArgsConstructor
public class AptitudeScoreController {
    private final AptitudeScoreRepository repository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Result<List<AptitudeScore>>> all() {
        return ResponseEntity.ok(Result.success("api.success.read.aptitudeScores.list", repository.findAll()));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Result<List<AptitudeScore>>> byStudent(@PathVariable("studentId") String studentId, Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, ApiMessages.get("api.error.authentication.userNotFound")));
        if (user.getRole() != com.placement.entity.User.Role.STUDENT || user.getStudent() == null || !user.getStudent().getId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ApiMessages.get("api.error.authentication.studentScoresForbidden"));
        }
        return ResponseEntity.ok(Result.success("api.success.read.aptitudeScores.student", repository.findByStudent_IdOrderByTest_TestDateDesc(studentId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<AptitudeScore>> one(@PathVariable("id") String id) {
        AptitudeScore score = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(ApiMessages.get("api.error.aptitudeScore.notFound", id)));
        return ResponseEntity.ok(Result.success("api.success.read.aptitudeScores.one", score));
    }

    @PostMapping
    public ResponseEntity<Result<AptitudeScore>> create(@Valid @RequestBody AptitudeScoreRequest request) {
        AptitudeScore value = toEntity(request);
        AptitudeScore saved = repository.save(value);
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeScore.created", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<AptitudeScore>> update(@PathVariable("id") String id, @Valid @RequestBody AptitudeScoreRequest request) {
        AptitudeScore value = toEntity(request);
        value.setId(id);
        AptitudeScore saved = repository.save(value);
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeScore.updated", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ApiMessages.get("api.error.aptitudeScore.notFound", id));
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeScore.deleted", null));
    }

    private AptitudeScore toEntity(AptitudeScoreRequest request) {
        return AptitudeScore.builder()
            .student(Student.builder().id(request.student().id()).build())
            .test(AptitudeTest.builder().id(request.test().id()).build())
            .score(request.score())
            .build();
    }
}
