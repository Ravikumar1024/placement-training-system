package com.placement.serviceimpl;

import com.placement.dto.AptitudeSubmissionRequest;
import com.placement.entity.AptitudeAttemptAnswer;
import com.placement.entity.AptitudeQuestion;
import com.placement.entity.AptitudeScore;
import com.placement.entity.AptitudeTest;
import com.placement.entity.Student;
import com.placement.entity.User;
import com.placement.repository.AptitudeQuestionRepository;
import com.placement.repository.AptitudeAttemptAnswerRepository;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.AptitudeTestRepository;
import com.placement.repository.StudentRepository;
import com.placement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AptitudeAssessmentServiceTest {
    @Mock private AptitudeQuestionRepository questionRepository;
    @Mock private AptitudeAttemptAnswerRepository attemptAnswerRepository;
    @Mock private AptitudeTestRepository testRepository;
    @Mock private AptitudeScoreRepository scoreRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AptitudeAssessmentService service;

    private AptitudeTest test;
    private Student student;
    private User user;
    private List<AptitudeQuestion> questions;

    @BeforeEach
    void setUp() {
        test = new AptitudeTest();
        test.setId("test-id");
        test.setTestName("Logic");
        test.setTotalMarks(20.0);
        student = new Student();
        student.setId("student-id");
        user = new User();
        user.setUsername("student-user");
        user.setRole(User.Role.STUDENT);
        user.setStudent(student);
        student.setUser(user);
        questions = List.of(question("q1", "A", 2), question("q2", "C", 1));
        when(testRepository.findById("test-id")).thenReturn(Optional.of(test));
        lenient().when(studentRepository.findById("student-id")).thenReturn(Optional.of(student));
        lenient().when(userRepository.findByUsername("student-user")).thenReturn(Optional.of(user));
        lenient().when(questionRepository.findByTest_IdOrderByIdAsc("test-id")).thenReturn(questions);
        lenient().when(scoreRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void gradesAnswersAndPersistsNormalizedScore() {
        var request = new AptitudeSubmissionRequest("student-id", List.of(
            new AptitudeSubmissionRequest.Answer("q1", "A"),
            new AptitudeSubmissionRequest.Answer("q2", "B")
        ));

        var result = service.submit("test-id", request, "student-user");

        assertEquals(13.33, result.earnedMarks());
        assertEquals(20.0, result.totalMarks());
        assertEquals(66.67, result.score());
        org.mockito.ArgumentCaptor<AptitudeScore> scoreCaptor = org.mockito.ArgumentCaptor.forClass(AptitudeScore.class);
        verify(scoreRepository).save(scoreCaptor.capture());
        assertEquals(true, scoreCaptor.getValue().isCompletedAttempt());
        assertEquals(13.33, scoreCaptor.getValue().getAttemptEarnedMarks());
        verify(attemptAnswerRepository).saveAll(any());
    }

    @Test
    void rejectsIncompleteAnswerSet() {
        var request = new AptitudeSubmissionRequest("student-id", List.of(
            new AptitudeSubmissionRequest.Answer("q1", "A")
        ));

        assertThrows(ResponseStatusException.class, () -> service.submit("test-id", request, "student-user"));
    }

    @Test
    void rejectsSecondCompletedAttempt() {
        when(scoreRepository.existsByStudent_IdAndTest_IdAndCompletedAttemptTrue("student-id", "test-id")).thenReturn(true);
        var request = new AptitudeSubmissionRequest("student-id", List.of(
            new AptitudeSubmissionRequest.Answer("q1", "A"),
            new AptitudeSubmissionRequest.Answer("q2", "C")
        ));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.submit("test-id", request, "student-user"));

        assertEquals(org.springframework.http.HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void reviewReturnsSavedQuestionsAndBothAnswersForCompletedAttempt() {
        AptitudeScore score = AptitudeScore.builder().id("score-id").student(student).test(test).score(66.67).completedAttempt(true).build();
        AptitudeAttemptAnswer answer = new AptitudeAttemptAnswer();
        answer.setQuestionOrder(0);
        answer.setPrompt("Question q1");
        answer.setOptionA("A");
        answer.setOptionB("B");
        answer.setOptionC("C");
        answer.setOptionD("D");
        answer.setSelectedOption("B");
        answer.setCorrectOption("A");
        answer.setMarks(2.0);
        when(scoreRepository.findFirstByStudent_IdAndTest_IdAndCompletedAttemptTrue("student-id", "test-id"))
            .thenReturn(Optional.of(score));
        when(attemptAnswerRepository.findByScore_IdOrderByQuestionOrderAsc("score-id"))
            .thenReturn(List.of(answer));

        var review = service.review("test-id", "student-user");

        assertEquals("Question q1", review.answers().getFirst().prompt());
        assertEquals("B", review.answers().getFirst().selectedOption());
        assertEquals("A", review.answers().getFirst().correctOption());
    }

    @Test
    void rejectsSubmittingForAnotherStudent() {
        var request = new AptitudeSubmissionRequest("another-student", List.of(
            new AptitudeSubmissionRequest.Answer("q1", "A"),
            new AptitudeSubmissionRequest.Answer("q2", "C")
        ));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.submit("test-id", request, "student-user"));

        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void studentQuestionViewDoesNotContainAnswerKey() {
        var views = service.studentQuestions("test-id");

        assertEquals(2, views.size());
        assertEquals("Question q1", views.getFirst().prompt());
    }

    private AptitudeQuestion question(String id, String correctOption, double marks) {
        AptitudeQuestion question = new AptitudeQuestion();
        question.setId(id);
        question.setTest(test);
        question.setPrompt("Question " + id);
        question.setOptionA("A");
        question.setOptionB("B");
        question.setOptionC("C");
        question.setOptionD("D");
        question.setCorrectOption(correctOption);
        question.setMarks(marks);
        return question;
    }
}