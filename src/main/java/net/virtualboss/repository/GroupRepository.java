package net.virtualboss.repository;

import net.virtualboss.model.entity.Group;
import net.virtualboss.model.enums.EntityType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GroupRepository extends JpaRepository<Group, Short> {
    Optional<Group> findByNameIgnoreCaseAndType(String name, EntityType type);
    Set<Group> findAllByType(EntityType type, Sort sort);
    Set<Group> findGroupsByIdInAndType(Collection<Short> id, EntityType type, Sort sort);
}
