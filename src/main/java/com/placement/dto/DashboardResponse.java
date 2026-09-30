package com.placement.dto;

import com.placement.entity.Attendance;
import com.placement.entity.Student;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
    String role,
    Map<String, Long> stats,
    Student student,
    List<PlacementStatusResponse> placements,
    List<Attendance> attendance,
    List<EligibilityResponse> eligibility
) {}
