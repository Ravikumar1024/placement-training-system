package com.placement.controller;

import com.placement.dto.DashboardResponse;
import com.placement.dto.EligibilityResponse;
import com.placement.dto.Result;
import com.placement.util.ApiMessages;
import com.placement.entity.Attendance;
import com.placement.entity.User;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.AttendanceRepository;
import com.placement.repository.CompanyRepository;
import com.placement.repository.PlacementRepository;
import com.placement.repository.StudentRepository;
import com.placement.repository.TrainingRepository;
import com.placement.repository.UserRepository;
import com.placement.service.PlacementService;
import com.placement.util.EligibilityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final TrainingRepository trainingRepository;
    private final PlacementRepository placementRepository;
    private final AptitudeScoreRepository aptitudeScoreRepository;
    private final AttendanceRepository attendanceRepository;
    private final PlacementService placementService;

    @GetMapping
    public ResponseEntity<Result<DashboardResponse>> dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ApiMessages.get("api.error.authentication.dashboardLoginRequired"));
        }
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, ApiMessages.get("api.error.authentication.userNotFound")));

        DashboardResponse response = user.getRole() == User.Role.ADMIN
            ? adminDashboard()
            : studentDashboard(user);
        return ResponseEntity.ok(Result.success("api.success.read.dashboard", response));
    }

    private DashboardResponse adminDashboard() {
        return new DashboardResponse(
            User.Role.ADMIN.name(),
            Map.of(
                "students", studentRepository.count(),
                "companies", companyRepository.count(),
                "trainings", trainingRepository.count(),
                "placements", placementRepository.count()
            ),
            null, null, null, null
        );
    }

    private DashboardResponse studentDashboard(User user) {
        if (user.getStudent() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ApiMessages.get("api.error.authentication.studentAccountRequired"));
        }
        var student = studentRepository.findById(user.getStudent().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, ApiMessages.get("api.error.student.profileNotFound")));
        String studentId = student.getId();
        double aptitude = aptitudeScoreRepository.findAll().stream()
            .filter(score -> score.getStudent().getId().equals(studentId))
            .mapToDouble(score -> score.getScore() == null ? 0 : score.getScore())
            .max()
            .orElse(0);
        List<Attendance> attendance = attendanceRepository.findAll().stream()
            .filter(record -> record.getStudent().getId().equals(studentId))
            .toList();
        double attendancePercent = attendance.isEmpty() ? 0 :
            attendance.stream().filter(Attendance::isPresent).count() * 100.0 / attendance.size();
        List<EligibilityResponse> eligibility = companyRepository.findAll().stream()
            .map(company -> {
                List<String> reasons = EligibilityUtil.reasons(student, company, aptitude, attendancePercent);
                return new EligibilityResponse(company.getId(), company.getCompanyName(), reasons.isEmpty(), reasons);
            })
            .toList();

        return new DashboardResponse(
            User.Role.STUDENT.name(),
            null,
            student,
            placementService.findByStudentId(studentId),
            attendance,
            eligibility
        );
    }
}