package com.placement.repository;

import com.placement.entity.Placement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlacementRepository extends JpaRepository<Placement, String> {
    List<Placement> findByStudent_IdOrderByAppliedDateDesc(String studentId);
	long countByStudent_Id(String studentId);
	long countByCompany_Id(String companyId);

    List<Placement> findByStudent_IdAndCompany_Id(String studentId, String companyId);
}
