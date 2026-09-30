package com.placement.repository;

import com.placement.entity.AptitudeTest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AptitudeTestRepository extends JpaRepository<AptitudeTest, String> {
    
}
