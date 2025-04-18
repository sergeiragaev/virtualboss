package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Field;
import net.virtualboss.common.model.entity.FieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FieldValueRepository extends JpaRepository<FieldValue, Long> {
    Optional<FieldValue> findByFieldAndCustomValue(Field field, String value);
    List<FieldValue> findAllByFieldIs(Field field);
}
