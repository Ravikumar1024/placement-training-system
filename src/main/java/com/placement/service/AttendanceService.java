package com.placement.service;

import com.placement.entity.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    List<Attendance> findAll();
    Attendance findById(String id);
    Attendance save(Attendance entity);
    List<Attendance> saveForTrainingDate(String trainingId, LocalDate date, List<com.placement.dto.BulkAttendanceRequest.Entry> entries);
    Attendance recordVideoCompletion(String studentId, String trainingId, LocalDate date);
    void delete(String id);
}
