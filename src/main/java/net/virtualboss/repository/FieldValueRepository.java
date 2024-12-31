package net.virtualboss.repository;

import net.virtualboss.model.entity.Field;
import net.virtualboss.model.entity.FieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FieldValueRepository extends JpaRepository<FieldValue, Long> {
    Optional<FieldValue> findByFieldAndValue(Field field, String value);
}
