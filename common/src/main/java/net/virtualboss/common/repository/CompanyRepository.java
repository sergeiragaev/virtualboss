package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    @Query("from Company c where lower(c.name) = 'unassigned' and c.isDeleted != true")
    Optional<Company> getUnassigned();

    @Modifying
    @Transactional
    @Query("UPDATE Company c SET c.isDeleted = true")
    int markAllAsDeleted();

    Optional<Company> findCompanyByNameEqualsIgnoreCase(String companyName);
}
