package com.placement.repository;

import com.placement.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrainingRepository extends JpaRepository<Training, String> {
    
}
