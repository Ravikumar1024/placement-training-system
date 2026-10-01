package com.placement.serviceimpl;

import com.placement.entity.Company;
import com.placement.repository.CompanyRepository;
import com.placement.repository.PlacementRepository;
import com.placement.service.CompanyService;
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
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository repository;
    private final PlacementRepository placementRepository;

    public List<Company> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Company findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("api.error.company.notFound", id));
    }

    @Transactional
    public Company save(Company entity) {
        if (entity.getCompanyName() != null && repository.findByCompanyName(entity.getCompanyName()).stream()
                .anyMatch(c -> !c.getId().equals(entity.getId()))) {
            throw new IllegalArgumentException(ApiMessages.get("api.error.company.nameExists", entity.getCompanyName()));
        }
        return repository.save(entity);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) 
            throw new ResourceNotFoundException("api.error.company.notFound", id);
        long placementCount = placementRepository.countByCompany_Id(id);
        if (placementCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                ApiMessages.get("api.error.company.placementReferences", placementCount));
        }
        repository.deleteById(id);
    }
}
