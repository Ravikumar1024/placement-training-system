package com.placement.repository;

import com.placement.entity.AptitudeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AptitudeQuestionRepository extends JpaRepository<AptitudeQuestion, String> {
    List<AptitudeQuestion> findByTest_IdOrderByIdAsc(String testId);
    long countByTest_Id(String testId);
}