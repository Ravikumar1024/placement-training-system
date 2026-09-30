package com.placement.serviceimpl;

import com.placement.entity.Student;
import com.placement.entity.User;
import com.placement.repository.StudentRepository;
import com.placement.repository.UserRepository;
import com.placement.service.StudentService;
import com.placement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Student> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Student findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    @Transactional
    public Student save(Student entity) {
        if (entity.getEmail() != null && repository.findByEmail(entity.getEmail()).stream()
                .anyMatch(s -> entity.getId() == null || !s.getId().equals(entity.getId()))) {
            throw new IllegalArgumentException("Email already exists: " + entity.getEmail());
        }

        Student existing = entity.getId() == null ? null : repository.findById(entity.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + entity.getId()));
        User account = existing == null ? null : existing.getUser();
        User usernameOwner = userRepository.findByUsername(entity.getCode()).orElse(null);
        if (usernameOwner != null && (account == null || !usernameOwner.getId().equals(account.getId()))) {
            throw new IllegalArgumentException("Student code already exists: " + entity.getCode());
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
        if (!repository.existsById(id)) 
            throw new ResourceNotFoundException("Student not found: " + id);
        repository.deleteById(id);
    }
}
