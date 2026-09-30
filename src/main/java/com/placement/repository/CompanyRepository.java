package com.placement.repository;

import com.placement.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String> {
    List<Company> findByCompanyName(String companyName);
    List<Company> findByJobRole(String jobRole);
    List<Company> findByPackageLpaBetween(Double min, Double max);
}
