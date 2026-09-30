package com.placement.service;

import com.placement.entity.Company;
import java.util.List;

public interface CompanyService {
    List<Company> findAll();
    Company findById(String id);
    Company save(Company entity);
    void delete(String id);
}
