package com.placement.repository;

import com.placement.entity.AptitudeScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AptitudeScoreRepository extends JpaRepository<AptitudeScore, String> {
	long countByTest_Id(String testId);
	long countByStudent_Id(String studentId);
	java.util.List<AptitudeScore> findByStudent_IdOrderByTest_TestDateDesc(String studentId);
	boolean existsByStudent_IdAndTest_IdAndCompletedAttemptTrue(String studentId, String testId);
	java.util.Optional<AptitudeScore> findFirstByStudent_IdAndTest_IdAndCompletedAttemptTrue(String studentId, String testId);
}
