package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    @Query("from Company c where lower(c.name) = 'unassigned'")
    Optional<Company> getUnassigned();

}
