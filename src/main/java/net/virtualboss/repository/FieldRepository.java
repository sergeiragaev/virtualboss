package net.virtualboss.repository;

import net.virtualboss.model.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FieldRepository extends JpaRepository<Field, Long> {

    List<Field> findAllByNameIn(Collection<String> name);
}
