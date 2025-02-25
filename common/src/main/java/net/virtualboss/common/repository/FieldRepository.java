package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FieldRepository extends JpaRepository<Field, Integer> {

    List<Field> findAllByNameIn(Collection<String> name);
    Optional<Field> findByName(String name);
}
