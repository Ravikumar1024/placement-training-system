package com.placement.controller;

import com.placement.dto.EligibilityResponse;
import com.placement.dto.Result;
import com.placement.entity.*;
import com.placement.repository.*;
import com.placement.util.ApiMessages;
import com.placement.util.EligibilityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/eligibility")
@RequiredArgsConstructor
public class EligibilityController {
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final AptitudeScoreRepository aptitudeScoreRepository;
    private final AttendanceRepository attendanceRepository;

    @GetMapping("/{studentId}")
    public ResponseEntity<Result<List<EligibilityResponse>>> eligible(@PathVariable("studentId") String studentId) {
        Student s = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException(ApiMessages.get("api.error.student.notFound", studentId)));
        
        double aptitude = aptitudeScoreRepository.findAll().stream()
            .filter(x -> x.getStudent().getId().equals(studentId))
            .mapToDouble(x -> x.getScore() == null ? 0 : x.getScore()).max().orElse(0);
        
        List<Attendance> records = attendanceRepository.findAll().stream()
            .filter(x -> x.getStudent().getId().equals(studentId)).toList();
        double attendance = records.isEmpty() ? 0 :
            records.stream().filter(Attendance::isPresent).count() * 100.0 / records.size();

        List<EligibilityResponse> responses = companyRepository.findAll().stream().map(c -> {
            List<String> reasons = EligibilityUtil.reasons(s, c, aptitude, attendance);
            return new EligibilityResponse(c.getId(), c.getCompanyName(), reasons.isEmpty(), reasons);
        }).toList();
        
        return ResponseEntity.ok(Result.success("api.success.read.eligibility", responses));
    }
}
