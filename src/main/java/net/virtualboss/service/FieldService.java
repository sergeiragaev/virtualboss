package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.model.entity.Field;
import net.virtualboss.repository.FieldRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class FieldService {
    private final FieldRepository fieldRepository;

    public Map<String, String>[] getFieldCaptions(String fieldList) {
        String[] fields = fieldList.split(",");
        List<Field> fieldCaptions = fieldRepository.findAllByNameIn(Arrays.stream(fields).toList());
        Map<String, String> captions = new HashMap<>();
        for (Field field : fieldCaptions) {
            captions.put(field.getName(), field.getAlias());
        }
        Map<String, String>[] map = new Map[1];
        map[0] = captions;
        return map;
    }
}
