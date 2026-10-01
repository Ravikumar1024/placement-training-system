package com.placement.serviceimpl;

import com.placement.controller.AptitudeTestController;
import com.placement.repository.AttendanceRepository;
import com.placement.repository.AptitudeQuestionRepository;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.AptitudeTestRepository;
import com.placement.repository.CompanyRepository;
import com.placement.repository.PlacementRepository;
import com.placement.repository.StudentRepository;
import com.placement.repository.TrainingRepository;
import com.placement.repository.UserRepository;
import com.placement.entity.Student;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteDependencyTest {
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private AptitudeScoreRepository aptitudeScoreRepository;
    @Mock private PlacementRepository placementRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private AptitudeTestRepository aptitudeTestRepository;
    @Mock private AptitudeQuestionRepository aptitudeQuestionRepository;

    @InjectMocks private StudentServiceImpl studentService;
    @InjectMocks private TrainingServiceImpl trainingService;
    @InjectMocks private CompanyServiceImpl companyService;
    @InjectMocks private AptitudeTestController aptitudeTestController;

    @Test
    void studentDeleteExplainsAttendanceDependency() {
        when(studentRepository.findById("student-id")).thenReturn(Optional.of(new Student()));
        when(attendanceRepository.countByStudent_Id("student-id")).thenReturn(2L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> studentService.delete("student-id"));

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Cannot delete student: 2 attendance record(s) reference this student. Delete those records first.", exception.getReason());
        verify(studentRepository, never()).delete(org.mockito.ArgumentMatchers.any(Student.class));
    }

    @Test
    void trainingDeleteExplainsAttendanceDependency() {
        when(trainingRepository.existsById("training-id")).thenReturn(true);
        when(attendanceRepository.countByTraining_Id("training-id")).thenReturn(1L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> trainingService.delete("training-id"));

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Cannot delete training: 1 attendance record(s) reference this training. Delete those records first.", exception.getReason());
        verify(trainingRepository, never()).deleteById("training-id");
    }

    @Test
    void companyDeleteExplainsPlacementDependency() {
        when(companyRepository.existsById("company-id")).thenReturn(true);
        when(placementRepository.countByCompany_Id("company-id")).thenReturn(3L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> companyService.delete("company-id"));

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Cannot delete company: 3 placement record(s) reference this company. Delete those records first.", exception.getReason());
        verify(companyRepository, never()).deleteById("company-id");
    }

    @Test
    void aptitudeTestDeleteExplainsScoreDependency() {
        when(aptitudeTestRepository.existsById("test-id")).thenReturn(true);
        when(aptitudeScoreRepository.countByTest_Id("test-id")).thenReturn(1L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> aptitudeTestController.delete("test-id"));

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Cannot delete aptitude test: 1 aptitude score(s) reference this test. Delete those scores first.", exception.getReason());
        verify(aptitudeTestRepository, never()).deleteById("test-id");
    }
}