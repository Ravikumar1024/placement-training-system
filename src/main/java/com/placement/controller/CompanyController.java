package com.placement.controller;

import com.placement.dto.Result;
import com.placement.entity.Company;
import com.placement.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService service;

    @GetMapping
    public ResponseEntity<Result<List<Company>>> all() {
        return ResponseEntity.ok(Result.success("Companies retrieved successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Company>> one(@PathVariable("id") String id) {
        Company company = service.findById(id);
        return ResponseEntity.ok(Result.success("Company retrieved successfully", company));
    }

    @PostMapping
    public ResponseEntity<Result<Company>> create(@Valid @RequestBody Company value) {
        Company saved = service.save(value);
        return ResponseEntity.ok(Result.success("Company created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Company>> update(@PathVariable("id") String id, @Valid @RequestBody Company value) {
        value.setId(id);
        Company saved = service.save(value);
        return ResponseEntity.ok(Result.success("Company updated successfully", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok(Result.success("Company deleted successfully",null));
    }
}
