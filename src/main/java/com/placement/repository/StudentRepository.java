package com.placement.repository;

import com.placement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {
    List<Student> findByEmail(String email);
    Optional<Student> findByPhone(String phone);
    List<Student> findByDepartment(String department);
    List<Student> findByBatch(String batch);
}
