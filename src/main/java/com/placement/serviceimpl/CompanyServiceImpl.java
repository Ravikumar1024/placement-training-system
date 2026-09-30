package com.placement.serviceimpl;

import com.placement.entity.Company;
import com.placement.repository.CompanyRepository;
import com.placement.service.CompanyService;
import com.placement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository repository;

    public List<Company> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Company findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
    }

    @Transactional
    public Company save(Company entity) {
        if (entity.getCompanyName() != null && repository.findByCompanyName(entity.getCompanyName()).stream()
                .anyMatch(c -> !c.getId().equals(entity.getId()))) {
            throw new IllegalArgumentException("Company name already exists: " + entity.getCompanyName());
        }
        return repository.save(entity);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) 
            throw new ResourceNotFoundException("Company not found: " + id);
        repository.deleteById(id);
    }
}
