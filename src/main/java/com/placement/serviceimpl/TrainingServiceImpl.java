package com.placement.serviceimpl;

import com.placement.entity.Training;
import com.placement.repository.TrainingRepository;
import com.placement.service.TrainingService;
import com.placement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository repository;

    public List<Training> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Training findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Training not found: " + id));
    }

    @Transactional
    public Training save(Training entity) {
        // Validate required fields
        if (entity.getTrainingName() == null || entity.getTrainingName().isBlank()) {
            throw new IllegalArgumentException("Training name is required");
        }
        if (entity.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        return repository.save(entity);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) 
            throw new ResourceNotFoundException("Training not found: " + id);
        repository.deleteById(id);
    }
}
