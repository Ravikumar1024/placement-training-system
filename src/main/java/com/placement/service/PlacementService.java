package com.placement.service;

import com.placement.dto.PlacementRequest;
import com.placement.dto.PlacementStatusResponse;
import com.placement.dto.PlacementGenerationResult;
import com.placement.entity.Placement;
import java.util.List;

public interface PlacementService {
    List<Placement> findAll();
    Placement findById(String id);
    Placement save(Placement entity);
    Placement save(PlacementRequest request);
    Placement update(String id, PlacementRequest request);
    void delete(String id);
    List<PlacementStatusResponse> findByStudentId(String studentId);
    PlacementGenerationResult generateEligiblePlacements();
}
