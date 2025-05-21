package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {
    Optional<Job> findByNumberIgnoreCaseAndIsDeleted(String name, boolean isDeleted);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.isDeleted = true")
    int markAllAsDeleted();
}
