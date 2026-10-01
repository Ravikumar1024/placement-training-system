package com.placement.serviceimpl;

import com.placement.entity.Student;
import com.placement.entity.User;
import com.placement.repository.StudentRepository;
import com.placement.repository.UserRepository;
import com.placement.repository.AttendanceRepository;
import com.placement.repository.AptitudeScoreRepository;
import com.placement.repository.PlacementRepository;
import com.placement.service.StudentService;
import com.placement.exception.ResourceNotFoundException;
import com.placement.util.ApiMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final AptitudeScoreRepository aptitudeScoreRepository;
    private final PlacementRepository placementRepository;

    public List<Student> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Student findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.student.notFound", id));
    }

    @Transactional
    public Student save(Student entity) {
        if (entity.getEmail() != null && repository.findByEmail(entity.getEmail()).stream()
                .anyMatch(s -> entity.getId() == null || !s.getId().equals(entity.getId()))) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.student.emailExists", entity.getEmail()));
        }

        Student existing = entity.getId() == null ? null : repository.findById(entity.getId())
            .orElseThrow(() -> new ResourceNotFoundException("api.error.student.notFound", entity.getId()));
        User account = existing == null ? null : existing.getUser();
        User usernameOwner = userRepository.findByUsername(entity.getCode()).orElse(null);
        if (usernameOwner != null && (account == null || !usernameOwner.getId().equals(account.getId()))) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.student.codeExists", entity.getCode()));
        }

        if (account == null) {
            account = User.builder()
                .username(entity.getCode())
                .password(passwordEncoder.encode(entity.getCode() + "@123"))
                .role(User.Role.STUDENT)
                .build();
        } else {
            account.setUsername(entity.getCode());
        }
        entity.setUser(userRepository.save(account));
        return repository.save(entity);
    }

    @Transactional
    public void delete(String id) {
        Student student = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.student.notFound", id));
        long attendanceCount = attendanceRepository.countByStudent_Id(id);
        if (attendanceCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                ApiMessages.get("api.error.student.attendanceReferences", attendanceCount));
        }
        long scoreCount = aptitudeScoreRepository.countByStudent_Id(id);
        if (scoreCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                ApiMessages.get("api.error.student.scoreReferences", scoreCount));
        }
        long placementCount = placementRepository.countByStudent_Id(id);
        if (placementCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                ApiMessages.get("api.error.student.placementReferences", placementCount));
        }

        String userId = student.getUser() == null ? null : student.getUser().getId();
        repository.delete(student);
        if (userId != null) userRepository.deleteById(userId);
    }
}
