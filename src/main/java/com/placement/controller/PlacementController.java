package com.placement.controller;

import com.placement.dto.*;
import com.placement.entity.Placement;
import com.placement.service.PlacementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.placement.dto.PlacementGenerationResult;

@RestController
@RequestMapping("/api/placements")
@RequiredArgsConstructor
public class PlacementController {
    private final PlacementService service;

    @GetMapping
    public ResponseEntity<Result<List<Placement>>> all() {
        return ResponseEntity.ok(Result.success("Placements retrieved successfully", service.findAll()));
    }

    @PostMapping("/generate-eligible")
    public ResponseEntity<Result<PlacementGenerationResult>> generateEligible() {
        PlacementGenerationResult result = service.generateEligiblePlacements();
        return ResponseEntity.ok(Result.success("Eligible placement records generated", result));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Result<List<PlacementStatusResponse>>> byStudent(@PathVariable("studentId") String studentId) {
        List<PlacementStatusResponse> placements = service.findByStudentId(studentId);
        return ResponseEntity.ok(Result.success("Placement status retrieved successfully", placements));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Placement>> one(@PathVariable("id") String id) {
        Placement placement = service.findById(id);
        return ResponseEntity.ok(Result.success("Placement retrieved successfully", placement));
    }

    @PostMapping
    public ResponseEntity<Result<Placement>> create(@Valid @RequestBody PlacementRequest request) {
        Placement saved = service.save(request);
        return ResponseEntity.ok(Result.success("Placement created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Placement>> update(@PathVariable("id") String id, @Valid @RequestBody PlacementRequest request) {
        Placement saved = service.update(id, request);
        return ResponseEntity.ok(Result.success("Placement updated successfully", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok(Result.success("Placement deleted successfully",null));
    }
}
