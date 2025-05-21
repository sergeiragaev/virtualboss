package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Group;
import net.virtualboss.common.model.enums.EntityType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    Optional<Group> findByNameIgnoreCaseAndType(String name, EntityType type);
    Set<Group> findAllByType(EntityType type, Sort sort);
    Set<Group> findGroupsByIdInAndType(Collection<UUID> id, EntityType type, Sort sort);

    @Modifying
    @Transactional
    @Query("UPDATE Group g SET g.isDeleted = true")
    int markAllAsDeleted();
}
