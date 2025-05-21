package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Profession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, UUID> {

    Optional<Profession> findProfessionByNameIgnoreCase(String companyName);

    @Modifying
    @Transactional
    @Query("UPDATE Profession p SET p.isDeleted = true")
    int markAllAsDeleted();

}
