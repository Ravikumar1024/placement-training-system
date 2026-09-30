package com.placement.service;

import com.placement.entity.Student;
import java.util.List;

public interface StudentService {
    List<Student> findAll();
    Student findById(String id);
    Student save(Student entity);
    void delete(String id);
}
