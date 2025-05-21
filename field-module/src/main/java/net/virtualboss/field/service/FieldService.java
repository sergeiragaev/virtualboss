package net.virtualboss.field.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.field.mapper.v1.FieldMapperV1;
import net.virtualboss.common.model.entity.Field;
import net.virtualboss.common.repository.FieldRepository;
import net.virtualboss.common.util.BeanUtils;
import net.virtualboss.field.web.dto.FieldDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class FieldService {
    private final FieldRepository fieldRepository;
    private final FieldMapperV1 fieldMapper;

    @Cacheable(value = "fieldCaptions", key = "#fieldList")
    public Map<String, String> getFieldCaptions(String fieldList) {
        String[] fields = fieldList.split(",");
        List<Field> fieldCaptions = fieldRepository.findAllByNameIn(Arrays.stream(fields).toList());

        return fieldCaptions.stream()
                .sorted(Comparator.comparing(Field::getAlias))
                .collect(Collectors.toMap(
                        Field::getName,
                        Field::getAlias,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));
    }

    public void updateField(FieldDto fieldDto) {

        Field field = fieldMapper.mapToField(fieldDto);

        Field fieldFromDb = fieldRepository.findByName(fieldDto.getName())
                .orElse(field);

        BeanUtils.copyNonNullProperties(field, fieldFromDb);

        fieldRepository.save(fieldFromDb);
    }
}
