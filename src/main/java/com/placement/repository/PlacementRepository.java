package com.placement.repository;

import com.placement.entity.Placement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlacementRepository extends JpaRepository<Placement, String> {
    List<Placement> findByStudent_IdOrderByAppliedDateDesc(String studentId);
    
    List<Placement> findByStudent_IdAndCompany_Id(String studentId, String companyId);
}
