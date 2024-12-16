package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.model.entity.Field;
import net.virtualboss.repository.FieldRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class FieldService {
    private final FieldRepository fieldRepository;

    @Cacheable(value = "field", key = "#fieldList")
    public List<Map<String, String>> getFieldCaptions(String fieldList) {
        String[] fields = fieldList.split(",");
        List<Field> fieldCaptions = fieldRepository.findAllByNameIn(Arrays.stream(fields).toList());
        Map<String, String> captions = new HashMap<>();
        for (Field field : fieldCaptions) {
            captions.put(field.getName(), field.getAlias());
        }
        List<Map<String, String>>  result = new ArrayList<>();
        result.add(captions);
        return result;
    }
}
