package com.placement.serviceimpl;

import com.placement.dto.AptitudeQuestionAdminView;
import com.placement.dto.AptitudeAnswerReview;
import com.placement.dto.AptitudeQuestionRequest;
import com.placement.dto.AptitudeQuestionImportRequest;
import com.placement.dto.AptitudeQuestionView;
import com.placement.dto.AptitudeSubmissionRequest;
import com.placement.dto.AptitudeSubmissionResult;
import com.placement.dto.AptitudeReviewView;
import com.placement.entity.AptitudeAttemptAnswer;
import com.placement.entity.AptitudeQuestion;
import com.placement.entity.AptitudeScore;
import com.placement.repository.AptitudeQuestionRepository;
import com.placement.repository.AptitudeAttemptAnswerRepository;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.AptitudeTestRepository;
import com.placement.repository.StudentRepository;
import com.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AptitudeAssessmentService {
    private final AptitudeQuestionRepository questionRepository;
    private final AptitudeAttemptAnswerRepository attemptAnswerRepository;
    private final AptitudeTestRepository testRepository;
    private final AptitudeScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AptitudeQuestionView> studentQuestions(String testId) {
        requireTest(testId);
        return questionRepository.findByTest_IdOrderByIdAsc(testId).stream()
            .map(question -> new AptitudeQuestionView(question.getId(), question.getPrompt(), question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD(), question.getMarks()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AptitudeQuestionAdminView> adminQuestions(String testId) {
        requireTest(testId);
        return questionRepository.findByTest_IdOrderByIdAsc(testId).stream()
            .map(question -> new AptitudeQuestionAdminView(question.getId(), question.getPrompt(), question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD(), question.getCorrectOption(), question.getMarks()))
            .toList();
    }

    @Transactional
    public AptitudeQuestionAdminView saveQuestion(String testId, String questionId, AptitudeQuestionRequest request) {
        var test = requireTest(testId);
        if (test.getTotalMarks() == null || test.getTotalMarks() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Set total marks greater than zero before adding questions.");
        }
        AptitudeQuestion question;
        if (questionId == null) {
            question = new AptitudeQuestion();
        } else {
            question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found."));
            if (!question.getTest().getId().equals(testId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question does not belong to this test.");
            }
        }
        question.setTest(test);
        question.setPrompt(request.prompt().trim());
        question.setOptionA(request.optionA().trim());
        question.setOptionB(request.optionB().trim());
        question.setOptionC(request.optionC().trim());
        question.setOptionD(request.optionD().trim());
        question.setCorrectOption(request.correctOption());
        question.setMarks(request.marks());
        question = questionRepository.save(question);
        return new AptitudeQuestionAdminView(question.getId(), question.getPrompt(), question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD(), question.getCorrectOption(), question.getMarks());
    }

    @Transactional
    public List<AptitudeQuestionAdminView> importQuestions(String testId, AptitudeQuestionImportRequest request) {
        var test = requireTest(testId);
        if (test.getTotalMarks() == null || test.getTotalMarks() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Set total marks greater than zero before importing questions.");
        }
        List<AptitudeQuestion> questions = request.questions().stream().map(item -> {
            AptitudeQuestion question = new AptitudeQuestion();
            question.setTest(test);
            question.setPrompt(item.prompt().trim());
            question.setOptionA(item.optionA().trim());
            question.setOptionB(item.optionB().trim());
            question.setOptionC(item.optionC().trim());
            question.setOptionD(item.optionD().trim());
            question.setCorrectOption(item.correctOption());
            question.setMarks(item.marks());
            return question;
        }).toList();
        List<AptitudeQuestion> saved = questionRepository.saveAll(questions);
        return saved.stream()
            .map(question -> new AptitudeQuestionAdminView(question.getId(), question.getPrompt(), question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD(), question.getCorrectOption(), question.getMarks()))
            .toList();
    }

    @Transactional
    public void deleteQuestion(String testId, String questionId) {
        AptitudeQuestion question = questionRepository.findById(questionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found."));
        if (!question.getTest().getId().equals(testId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question does not belong to this test.");
        }
        questionRepository.delete(question);
    }

    @Transactional
    public AptitudeSubmissionResult submit(String testId, AptitudeSubmissionRequest request, String username) {
        var test = requireTest(testId);
        var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
        if (user.getRole() != com.placement.entity.User.Role.STUDENT || user.getStudent() == null || !user.getStudent().getId().equals(request.studentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Students may only submit tests for their own account.");
        }
        var student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
        if (scoreRepository.existsByStudent_IdAndTest_IdAndCompletedAttemptTrue(student.getId(), testId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already completed this test.");
        }
        List<AptitudeQuestion> questions = questionRepository.findByTest_IdOrderByIdAsc(testId);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This test does not have any questions yet.");
        }
        if (test.getTotalMarks() == null || test.getTotalMarks() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This test has no valid total marks.");
        }
        Map<String, AptitudeSubmissionRequest.Answer> answers = request.answers().stream()
            .collect(Collectors.toMap(AptitudeSubmissionRequest.Answer::questionId, Function.identity(), (first, duplicate) -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A question was answered more than once.");
            }));
        if (answers.size() != questions.size() || questions.stream().anyMatch(question -> !answers.containsKey(question.getId()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer every question exactly once before submitting.");
        }
        double possibleQuestionMarks = questions.stream().mapToDouble(AptitudeQuestion::getMarks).sum();
        double earnedQuestionMarks = questions.stream()
            .filter(question -> question.getCorrectOption().equals(answers.get(question.getId()).selectedOption()))
            .mapToDouble(AptitudeQuestion::getMarks)
            .sum();
        double percentage = round(earnedQuestionMarks * 100.0 / possibleQuestionMarks);
        double earnedMarks = round(test.getTotalMarks() * percentage / 100.0);

        AptitudeScore score = scoreRepository.save(AptitudeScore.builder()
            .student(student)
            .test(test)
            .score(percentage)
            .completedAttempt(true)
            .attemptEarnedMarks(earnedMarks)
            .attemptTotalMarks(test.getTotalMarks())
            .build());
        List<AptitudeAttemptAnswer> attemptAnswers = IntStream.range(0, questions.size()).mapToObj(questionOrder -> {
            AptitudeQuestion question = questions.get(questionOrder);
            AptitudeAttemptAnswer answer = new AptitudeAttemptAnswer();
            answer.setScore(score);
            answer.setQuestionId(question.getId());
            answer.setQuestionOrder(questionOrder);
            answer.setPrompt(question.getPrompt());
            answer.setOptionA(question.getOptionA());
            answer.setOptionB(question.getOptionB());
            answer.setOptionC(question.getOptionC());
            answer.setOptionD(question.getOptionD());
            answer.setSelectedOption(answers.get(question.getId()).selectedOption());
            answer.setCorrectOption(question.getCorrectOption());
            answer.setMarks(question.getMarks());
            return answer;
        }).toList();
        attemptAnswerRepository.saveAll(attemptAnswers);
        return new AptitudeSubmissionResult(score.getId(), test.getTestName(), earnedMarks, test.getTotalMarks(), percentage);
    }

    @Transactional(readOnly = true)
    public AptitudeReviewView review(String testId, String username) {
        var test = requireTest(testId);
        var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
        if (user.getRole() != com.placement.entity.User.Role.STUDENT || user.getStudent() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A linked student account is required to review a test.");
        }
        AptitudeScore score = scoreRepository.findFirstByStudent_IdAndTest_IdAndCompletedAttemptTrue(user.getStudent().getId(), testId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complete this test before viewing its answers."));
        List<AptitudeAnswerReview> answers = attemptAnswerRepository.findByScore_IdOrderByQuestionOrderAsc(score.getId()).stream()
            .map(answer -> new AptitudeAnswerReview(
                answer.getPrompt(),
                List.of(answer.getOptionA(), answer.getOptionB(), answer.getOptionC(), answer.getOptionD()),
                answer.getSelectedOption(),
                answer.getCorrectOption(),
                answer.getMarks()))
            .toList();
        if (answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The saved attempt has no answer review data.");
        }
        double totalMarks = score.getAttemptTotalMarks() == null ? test.getTotalMarks() : score.getAttemptTotalMarks();
        double earnedMarks = score.getAttemptEarnedMarks() == null ? round(totalMarks * score.getScore() / 100.0) : score.getAttemptEarnedMarks();
        return new AptitudeReviewView(test.getTestName(), earnedMarks, totalMarks, score.getScore(), answers);
    }

    private com.placement.entity.AptitudeTest requireTest(String testId) {
        return testRepository.findById(testId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aptitude test not found."));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}