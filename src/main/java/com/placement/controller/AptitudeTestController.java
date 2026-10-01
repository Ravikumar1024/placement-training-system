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
import com.placement.util.ApiMessages;
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
        return ResponseEntity.ok(Result.success("api.success.read.aptitudeTests.list", tests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<AptitudeTest>> one(@PathVariable("id") String id) {
        AptitudeTest test = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(ApiMessages.get("api.error.aptitude.testNotFound", id)));
        return ResponseEntity.ok(Result.success("api.success.read.aptitudeTests.one", test));
    }

    @PostMapping
    public ResponseEntity<Result<AptitudeTest>> create(@Valid @RequestBody AptitudeTestRequest request) {
        AptitudeTest value = toEntity(request);
        AptitudeTest saved = repository.save(value);
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeTest.created", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<AptitudeTest>> update(@PathVariable("id") String id, @Valid @RequestBody AptitudeTestRequest request) {
        AptitudeTest value = toEntity(request);
        value.setId(id);
        AptitudeTest saved = repository.save(value);
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeTest.updated", saved));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<Result<List<AptitudeQuestionView>>> questions(@PathVariable("id") String id) {
        return ResponseEntity.ok(Result.success("api.success.read.aptitudeQuestions.student", assessmentService.studentQuestions(id)));
    }

    @GetMapping("/{id}/questions/manage")
    public ResponseEntity<Result<List<AptitudeQuestionAdminView>>> manageQuestions(@PathVariable("id") String id) {
        return ResponseEntity.ok(Result.success("api.success.read.aptitudeQuestions.admin", assessmentService.adminQuestions(id)));
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<Result<AptitudeQuestionAdminView>> addQuestion(@PathVariable("id") String id, @Valid @RequestBody AptitudeQuestionRequest request) {
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeQuestion.created", assessmentService.saveQuestion(id, null, request)));
    }

    @PostMapping("/{id}/questions/import")
    public ResponseEntity<Result<List<AptitudeQuestionAdminView>>> importQuestions(@PathVariable("id") String id, @Valid @RequestBody AptitudeQuestionImportRequest request) {
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeQuestions.imported", assessmentService.importQuestions(id, request)));
    }

    @PutMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Result<AptitudeQuestionAdminView>> updateQuestion(@PathVariable("id") String id, @PathVariable("questionId") String questionId, @Valid @RequestBody AptitudeQuestionRequest request) {
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeQuestion.updated", assessmentService.saveQuestion(id, questionId, request)));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Result<Void>> deleteQuestion(@PathVariable("id") String id, @PathVariable("questionId") String questionId) {
        assessmentService.deleteQuestion(id, questionId);
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeQuestion.deleted", null));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Result<AptitudeSubmissionResult>> submit(@PathVariable("id") String id, @Valid @RequestBody AptitudeSubmissionRequest request, Authentication authentication) {
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeTest.submitted", assessmentService.submit(id, request, authentication.getName())));
    }

    @GetMapping("/{id}/review")
    public ResponseEntity<Result<AptitudeReviewView>> review(@PathVariable("id") String id, Authentication authentication) {
        return ResponseEntity.ok(Result.success("api.success.read.aptitudeTest.review", assessmentService.review(id, authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ApiMessages.get("api.error.aptitude.testNotFound", id));
        }
        long scoreCount = scoreRepository.countByTest_Id(id);
        if (scoreCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                ApiMessages.get("api.error.aptitudeTest.scoreReferences", scoreCount));
        }
        long questionCount = questionRepository.countByTest_Id(id);
        if (questionCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                ApiMessages.get("api.error.aptitudeTest.questionReferences", questionCount));
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Result.success("api.success.write.aptitudeTest.deleted", null));
    }

    private AptitudeTest toEntity(AptitudeTestRequest request) {
        return AptitudeTest.builder()
            .testName(request.testName())
            .testDate(request.testDate())
            .totalMarks(request.totalMarks())
            .build();
    }
}
