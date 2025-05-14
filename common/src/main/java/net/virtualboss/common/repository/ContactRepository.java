package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID>, JpaSpecificationExecutor<Contact> {

    @Query("select cn from Contact cn left join cn.company cp where lower(cp.name) = 'unassigned'")
    Optional<Contact> getUnassigned();
}
