package com.placement.serviceimpl;

import com.placement.dto.PlacementRequest;
import com.placement.dto.PlacementStatusResponse;
import com.placement.dto.PlacementGenerationResult;
import com.placement.entity.Attendance;
import com.placement.entity.AptitudeScore;
import com.placement.entity.Company;
import com.placement.entity.Placement;
import com.placement.entity.Student;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.CompanyRepository;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.AttendanceRepository;
import com.placement.repository.PlacementRepository;
import com.placement.repository.StudentRepository;
import com.placement.service.PlacementService;
import com.placement.util.EligibilityUtil;
import com.placement.util.ApiMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlacementServiceImpl implements PlacementService {
    private final PlacementRepository repository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final AptitudeScoreRepository aptitudeScoreRepository;
    private final AttendanceRepository attendanceRepository;
    private final Clock clock;

    public List<Placement> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Placement findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.placement.notFound", id));
    }

    @Transactional
    public Placement save(Placement entity) { 
        return repository.save(entity); 
    }

    @Override
    @Transactional
    public Placement save(PlacementRequest request) {
        Student student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new ResourceNotFoundException("api.error.student.notFound", request.studentId()));
        Company company = companyRepository.findById(request.companyId())
            .orElseThrow(() -> new ResourceNotFoundException("api.error.company.notFound", request.companyId()));

        // Check if placement already exists for this student and company
        if (repository.findByStudent_IdAndCompany_Id(request.studentId(), request.companyId()).stream()
                .anyMatch(p -> p.getStatus() != Placement.Status.REJECTED)) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.placement.alreadyExists"));
        }

        Placement placement = Placement.builder()
            .student(student)
            .company(company)
            .status(request.status() != null ? request.status() : Placement.Status.APPLIED)
            .appliedDate(request.appliedDate() != null ? request.appliedDate() : java.time.LocalDate.now())
            .placementDate(request.placementDate())
            .build();
        return repository.save(placement);
    }

    @Override
    @Transactional
    public Placement update(String id, PlacementRequest request) {
        Placement placement = findById(id);
        Student student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new ResourceNotFoundException("api.error.student.notFound", request.studentId()));
        Company company = companyRepository.findById(request.companyId())
            .orElseThrow(() -> new ResourceNotFoundException("api.error.company.notFound", request.companyId()));
        
        placement.setStudent(student);
        placement.setCompany(company);
        placement.setStatus(request.status() != null ? request.status() : placement.getStatus());
        placement.setAppliedDate(request.appliedDate() != null ? request.appliedDate() : placement.getAppliedDate());
        placement.setPlacementDate(request.placementDate());
        return repository.save(placement);
    }

    @Override
    @Transactional
    public PlacementGenerationResult generateEligiblePlacements() {
        List<Student> students = studentRepository.findAll();
        List<Company> companies = companyRepository.findAll();
        List<AptitudeScore> scores = aptitudeScoreRepository.findAll();
        List<Attendance> attendanceRecords = attendanceRepository.findAll();
        List<Placement> existingPlacements = repository.findAll();

        Map<String, Double> bestAptitudeByStudent = new HashMap<>();
        for (AptitudeScore score : scores) {
            String studentId = score.getStudent().getId();
            double value = score.getScore() == null ? 0 : score.getScore();
            bestAptitudeByStudent.merge(studentId, value, Math::max);
        }

        Map<String, int[]> attendanceByStudent = new HashMap<>();
        for (Attendance attendance : attendanceRecords) {
            int[] counts = attendanceByStudent.computeIfAbsent(attendance.getStudent().getId(), key -> new int[2]);
            counts[1]++;
            if (attendance.isPresent()) counts[0]++;
        }

        Set<String> activePairs = new HashSet<>();
        for (Placement placement : existingPlacements) {
            if (placement.getStatus() != Placement.Status.REJECTED) {
                activePairs.add(pairKey(placement.getStudent().getId(), placement.getCompany().getId()));
            }
        }

        int created = 0;
        int skipped = 0;
        LocalDate today = LocalDate.now(clock);
        for (Student student : students) {
            double aptitude = bestAptitudeByStudent.getOrDefault(student.getId(), 0.0);
            int[] attendanceCounts = attendanceByStudent.get(student.getId());
            double attendance = attendanceCounts == null ? 0 : attendanceCounts[0] * 100.0 / attendanceCounts[1];
            for (Company company : companies) {
                if (!EligibilityUtil.reasons(student, company, aptitude, attendance).isEmpty()) continue;
                String pair = pairKey(student.getId(), company.getId());
                if (!activePairs.add(pair)) {
                    skipped++;
                    continue;
                }
                repository.save(Placement.builder()
                    .student(student)
                    .company(company)
                    .status(Placement.Status.APPLIED)
                    .appliedDate(today)
                    .build());
                created++;
            }
        }
        return new PlacementGenerationResult(created, skipped);
    }

    private String pairKey(String studentId, String companyId) {
        return studentId + "|" + companyId;
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("api.error.placement.notFound", id);
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlacementStatusResponse> findByStudentId(String studentId) {
        return repository.findByStudent_IdOrderByAppliedDateDesc(studentId).stream()
            .map(p -> new PlacementStatusResponse(
                p.getId(),
                p.getStudent().getId(),
                p.getCompany().getCompanyName(),
                p.getCompany().getJobRole(),
                p.getCompany().getPackageLpa(),
                p.getStatus(),
                p.getAppliedDate(),
                p.getPlacementDate()
            ))
            .toList();
    }
}
