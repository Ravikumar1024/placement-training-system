package com.placement.serviceimpl;

import com.placement.entity.Training;
import com.placement.repository.AttendanceRepository;
import com.placement.repository.TrainingRepository;
import com.placement.service.TrainingService;
import com.placement.exception.ResourceNotFoundException;
import com.placement.util.ApiMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository repository;
    private final AttendanceRepository attendanceRepository;

    public List<Training> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Training findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.training.notFound", id));
    }

    @Transactional
    public Training save(Training entity) {
        // Validate required fields
        if (entity.getTrainingName() == null || entity.getTrainingName().isBlank()) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.training.nameRequired"));
        }
        if (entity.getStartDate() == null) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.training.startDateRequired"));
        }
        return repository.save(entity);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) 
            throw new ResourceNotFoundException("api.error.training.notFound", id);
        long attendanceCount = attendanceRepository.countByTraining_Id(id);
        if (attendanceCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                ApiMessages.get("api.error.training.attendanceReferences", attendanceCount));
        }
        repository.deleteById(id);
    }
}
