package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByName(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.isDeleted = true")
    int markAllAsDeleted();

}
