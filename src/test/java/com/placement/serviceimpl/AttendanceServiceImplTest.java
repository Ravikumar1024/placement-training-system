package com.placement.serviceimpl;

import com.placement.entity.Attendance;
import com.placement.entity.Student;
import com.placement.entity.Training;
import com.placement.repository.AttendanceRepository;
import com.placement.repository.StudentRepository;
import com.placement.repository.TrainingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private TrainingRepository trainingRepository;
    @InjectMocks private AttendanceServiceImpl service;

    @Test
    void videoCompletionUpdatesExistingAttendanceForTheDay() {
        LocalDate today = LocalDate.now();
        Student student = Student.builder().id("student-id").build();
        Training training = Training.builder()
            .id("training-id")
            .trainingName("Java")
            .videoUrl("https://www.youtube.com/watch?v=abcdefghijk")
            .startDate(today.minusDays(1))
            .endDate(today.plusDays(1))
            .build();
        Attendance existing = Attendance.builder()
            .id("attendance-id")
            .student(student)
            .training(training)
            .date(today)
            .present(false)
            .build();
        when(trainingRepository.findById("training-id")).thenReturn(Optional.of(training));
        when(studentRepository.findById("student-id")).thenReturn(Optional.of(student));
        when(attendanceRepository.findFirstByStudent_IdAndTraining_IdAndDate("student-id", "training-id", today))
            .thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance saved = service.recordVideoCompletion("student-id", "training-id", today);

        assertSame(existing, saved);
        assertTrue(saved.isPresent());
        verify(attendanceRepository).save(existing);
    }
}