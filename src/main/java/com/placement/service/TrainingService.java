package com.placement.service;

import com.placement.entity.Training;
import java.util.List;

public interface TrainingService {
    List<Training> findAll();
    Training findById(String id);
    Training save(Training entity);
    void delete(String id);
}
