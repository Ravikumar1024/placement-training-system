package com.placement.repository;

import com.placement.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {
	Optional<Attendance> findFirstByStudent_IdAndTraining_IdAndDate(String studentId, String trainingId, LocalDate date);
}
