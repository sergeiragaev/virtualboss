package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID>, JpaSpecificationExecutor<Contact> {

    @Query("select cn from Contact cn left join cn.company cp where lower(cp.name) = 'unassigned' " +
           "and not cn.isDeleted and not cp.isDeleted")
    Optional<Contact> getUnassigned();

    @Modifying
    @Transactional
    @Query("UPDATE Contact c SET c.isDeleted = true")
    int markAllAsDeleted();

    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDeleted = true")
    int markAllAddressesAsDeleted();

    @Modifying
    @Transactional
    @Query("UPDATE Communication c SET c.isDeleted = true")
    int markAllCommunicationsAsDeleted();

}
