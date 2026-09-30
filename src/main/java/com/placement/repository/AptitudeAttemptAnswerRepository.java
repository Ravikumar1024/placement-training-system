package com.placement.repository;

import com.placement.entity.AptitudeAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AptitudeAttemptAnswerRepository extends JpaRepository<AptitudeAttemptAnswer, String> {
    List<AptitudeAttemptAnswer> findByScore_IdOrderByQuestionOrderAsc(String scoreId);
}