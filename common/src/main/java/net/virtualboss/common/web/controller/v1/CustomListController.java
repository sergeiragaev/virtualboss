package net.virtualboss.common.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.service.CustomFieldService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@CacheConfig(cacheNames = "custom_list")
@RequestMapping("/customList")
public class CustomListController {
    private final CustomFieldService service;


    @GetMapping("/{listName}")
    public ResponseEntity<List<String>> getValuesByListName(@PathVariable String listName) {
        return ResponseEntity.ok(service.getValuesByListName(listName));
    }
}
