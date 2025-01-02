package net.virtualboss.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.task.TaskFilter;
import net.virtualboss.service.TaskService;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@CacheConfig(cacheNames = "task")
public class TaskController {
    private final TaskService service;

    @GetMapping("/task")
    public ResponseEntity<List<Map<String, Object>>> getTasks(
            TaskFilter filter,
            @RequestParam String fields) {
        return ResponseEntity.ok(service.findAll(fields, filter));
    }

    @GetMapping("/taskdata")
    @ResponseStatus(HttpStatus.OK)
    public void taskData(@RequestParam boolean logincheck) {
    }

    @GetMapping("/task/{id}")
    public ResponseEntity<Map<String, Object>> taskDetails(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/task/{id}")
    public ResponseEntity<Map<String, Object>> saveTask(
            @PathVariable String id,
            UpsertTaskRequest request,
            CustomFieldsAndLists customFieldsAndLists) {
        return ResponseEntity.ok(service.saveTask(id, request, customFieldsAndLists));
    }

    @PostMapping("/task")
    public ResponseEntity<Map<String, Object>> createTask(
            UpsertTaskRequest request,
            CustomFieldsAndLists customFieldsAndLists) {
        return new ResponseEntity<>(
                service.createNewTask(request, customFieldsAndLists), HttpStatus.CREATED);
    }

    @DeleteMapping("/task/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTask(@PathVariable String id) {
        service.deleteTaskById(id);
    }
}
