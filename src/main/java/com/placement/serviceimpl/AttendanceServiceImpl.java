package com.placement.serviceimpl;

import com.placement.entity.Attendance;
import com.placement.repository.StudentRepository;
import com.placement.repository.TrainingRepository;
import com.placement.repository.AttendanceRepository;
import com.placement.service.AttendanceService;
import com.placement.exception.ResourceNotFoundException;
import com.placement.util.ApiMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository repository;
    private final StudentRepository studentRepository;
    private final TrainingRepository trainingRepository;

    public List<Attendance> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Attendance findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.attendance.notFound", id));
    }

    @Transactional
    public Attendance save(Attendance entity) {
        // Validate that student and training exist and are valid
        if (entity.getStudent() == null || entity.getStudent().getId() == null) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.attendance.studentRequired"));
        }
        if (entity.getTraining() == null || entity.getTraining().getId() == null) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.attendance.trainingRequired"));
        }
        if (entity.getDate() == null) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.attendance.dateRequired"));
        }
        return repository.save(entity);
    }

    @Transactional
    public List<Attendance> saveForTrainingDate(String trainingId, LocalDate date, List<com.placement.dto.BulkAttendanceRequest.Entry> entries) {
        Set<String> studentIds = new HashSet<>();
        if (entries.stream().anyMatch(entry -> !studentIds.add(entry.studentId()))) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.attendance.duplicateStudent"));
        }
        var training = trainingRepository.findById(trainingId)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.training.notFound", trainingId));
        List<Attendance> records = entries.stream().map(entry -> {
            var student = studentRepository.findById(entry.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("api.error.student.notFound", entry.studentId()));
            Attendance attendance = repository.findFirstByStudent_IdAndTraining_IdAndDate(student.getId(), trainingId, date)
                .orElseGet(Attendance::new);
            attendance.setStudent(student);
            attendance.setTraining(training);
            attendance.setDate(date);
            attendance.setPresent(entry.present());
            return attendance;
        }).toList();
        return repository.saveAll(records);
    }

    @Transactional
    public Attendance recordVideoCompletion(String studentId, String trainingId, LocalDate date) {
        var training = trainingRepository.findById(trainingId)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.training.notFound", trainingId));
        if (training.getVideoUrl() == null || training.getVideoUrl().isBlank()) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.attendance.videoNotAssigned"));
        }
        if (date.isBefore(training.getStartDate()) || (training.getEndDate() != null && date.isAfter(training.getEndDate()))) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.attendance.outsideTrainingDates"));
        }
        var student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.student.notFound", studentId));
        Attendance attendance = repository.findFirstByStudent_IdAndTraining_IdAndDate(studentId, trainingId, date)
            .orElseGet(Attendance::new);
        attendance.setStudent(student);
        attendance.setTraining(training);
        attendance.setDate(date);
        attendance.setPresent(true);
        return repository.save(attendance);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) 
            throw new ResourceNotFoundException("api.error.attendance.notFound", id);
        repository.deleteById(id);
    }
}
