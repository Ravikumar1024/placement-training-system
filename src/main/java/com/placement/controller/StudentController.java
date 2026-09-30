package com.placement.controller;

import com.placement.dto.Result;
import com.placement.entity.Student;
import com.placement.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService service;

    @GetMapping
    public ResponseEntity<Result<List<Student>>> all() {
        return ResponseEntity.ok(Result.success("Students retrieved successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Student>> one(@PathVariable("id") String id) {
        Student student = service.findById(id);
        return ResponseEntity.ok(Result.success("Student retrieved successfully", student));
    }

    @PostMapping
    public ResponseEntity<Result<Student>> create(@Valid @RequestBody Student value) {
        Student saved = service.save(value);
        return ResponseEntity.ok(Result.success("Student created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Student>> update(@PathVariable("id") String id, @Valid @RequestBody Student value) {
        value.setId(id);
        Student saved = service.save(value);
        return ResponseEntity.ok(Result.success("Student updated successfully", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok(Result.success("Student deleted successfully",null));
    }
}
