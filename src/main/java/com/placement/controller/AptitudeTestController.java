package com.placement.controller;

import com.placement.dto.Result;
import com.placement.dto.AptitudeQuestionAdminView;
import com.placement.dto.AptitudeQuestionRequest;
import com.placement.dto.AptitudeQuestionImportRequest;
import com.placement.dto.AptitudeQuestionView;
import com.placement.dto.AptitudeSubmissionRequest;
import com.placement.dto.AptitudeSubmissionResult;
import com.placement.dto.AptitudeTestView;
import com.placement.dto.AptitudeTestRequest;
import com.placement.dto.AptitudeReviewView;
import com.placement.entity.AptitudeTest;
import com.placement.repository.AptitudeQuestionRepository;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.AptitudeTestRepository;
import org.springframework.security.core.Authentication;
import com.placement.serviceimpl.AptitudeAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/aptitude-tests")
@RequiredArgsConstructor
public class AptitudeTestController {
    private final AptitudeTestRepository repository;
    private final AptitudeQuestionRepository questionRepository;
    private final AptitudeScoreRepository scoreRepository;
    private final AptitudeAssessmentService assessmentService;

    @GetMapping
    public ResponseEntity<Result<List<AptitudeTestView>>> all() {
        List<AptitudeTestView> tests = repository.findAll().stream()
            .map(test -> new AptitudeTestView(test.getId(), test.getTestName(), test.getTestDate(), test.getTotalMarks(), questionRepository.countByTest_Id(test.getId())))
            .toList();
        return ResponseEntity.ok(Result.success("Aptitude tests retrieved successfully", tests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<AptitudeTest>> one(@PathVariable("id") String id) {
        AptitudeTest test = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Aptitude test not found: " + id));
        return ResponseEntity.ok(Result.success("Aptitude test retrieved successfully", test));
    }

    @PostMapping
    public ResponseEntity<Result<AptitudeTest>> create(@Valid @RequestBody AptitudeTestRequest request) {
        AptitudeTest value = toEntity(request);
        AptitudeTest saved = repository.save(value);
        return ResponseEntity.ok(Result.success("Aptitude test created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<AptitudeTest>> update(@PathVariable("id") String id, @Valid @RequestBody AptitudeTestRequest request) {
        AptitudeTest value = toEntity(request);
        value.setId(id);
        AptitudeTest saved = repository.save(value);
        return ResponseEntity.ok(Result.success("Aptitude test updated successfully", saved));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<Result<List<AptitudeQuestionView>>> questions(@PathVariable("id") String id) {
        return ResponseEntity.ok(Result.success("Aptitude questions retrieved successfully", assessmentService.studentQuestions(id)));
    }

    @GetMapping("/{id}/questions/manage")
    public ResponseEntity<Result<List<AptitudeQuestionAdminView>>> manageQuestions(@PathVariable String id) {
        return ResponseEntity.ok(Result.success("Aptitude questions retrieved successfully", assessmentService.adminQuestions(id)));
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<Result<AptitudeQuestionAdminView>> addQuestion(@PathVariable("id") String id, @Valid @RequestBody AptitudeQuestionRequest request) {
        return ResponseEntity.ok(Result.success("Aptitude question created successfully", assessmentService.saveQuestion(id, null, request)));
    }

    @PostMapping("/{id}/questions/import")
    public ResponseEntity<Result<List<AptitudeQuestionAdminView>>> importQuestions(@PathVariable("id") String id, @Valid @RequestBody AptitudeQuestionImportRequest request) {
        return ResponseEntity.ok(Result.success("Online aptitude questions imported successfully", assessmentService.importQuestions(id, request)));
    }

    @PutMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Result<AptitudeQuestionAdminView>> updateQuestion(@PathVariable("id") String id, @PathVariable String questionId, @Valid @RequestBody AptitudeQuestionRequest request) {
        return ResponseEntity.ok(Result.success("Aptitude question updated successfully", assessmentService.saveQuestion(id, questionId, request)));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Result<Void>> deleteQuestion(@PathVariable("id") String id, @PathVariable("questionId") String questionId) {
        assessmentService.deleteQuestion(id, questionId);
        return ResponseEntity.ok(Result.success("Aptitude question deleted successfully", null));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Result<AptitudeSubmissionResult>> submit(@PathVariable("id") String id, @Valid @RequestBody AptitudeSubmissionRequest request, Authentication authentication) {
        return ResponseEntity.ok(Result.success("Aptitude test submitted successfully", assessmentService.submit(id, request, authentication.getName())));
    }

    @GetMapping("/{id}/review")
    public ResponseEntity<Result<AptitudeReviewView>> review(@PathVariable("id") String id, Authentication authentication) {
        return ResponseEntity.ok(Result.success("Aptitude test review retrieved successfully", assessmentService.review(id, authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        if (scoreRepository.countByTest_Id(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A test with recorded scores cannot be deleted.");
        }
        if (questionRepository.countByTest_Id(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delete the test questions before deleting this test.");
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Result.success("Aptitude test deleted successfully",null));
    }

    private AptitudeTest toEntity(AptitudeTestRequest request) {
        return AptitudeTest.builder()
            .testName(request.testName())
            .testDate(request.testDate())
            .totalMarks(request.totalMarks())
            .build();
    }
}
