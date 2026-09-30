//package com.placement.serviceimpl;
//
//import java.util.List;
//
//import org.springframework.stereotype.Service;
//
//import com.placement.exception.ResourceNotFoundException;
//import com.placement.service.AptitudeService;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class AptitudeServiceImpl implements AptitudeService {
//    private final AptitudeRepository repository;
//
//    public List<Aptitude> findAll() { return repository.findAll(); }
//
//    public Aptitude findById(Long id) {
//        return repository.findById(id)
//            .orElseThrow(() -> new ResourceNotFoundException("Aptitude not found: " + id));
//    }
//
//    public Aptitude save(Aptitude entity) { return repository.save(entity); }
//
//    public void delete(Long id) {
//        if (!repository.existsById(id)) throw new ResourceNotFoundException("Aptitude not found: " + id);
//        repository.deleteById(id);
//    }
//}
