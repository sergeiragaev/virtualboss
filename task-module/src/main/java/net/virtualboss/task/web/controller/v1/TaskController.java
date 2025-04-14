package net.virtualboss.task.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.web.dto.TaskFilter;
import net.virtualboss.task.service.TaskService;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
import net.virtualboss.task.web.dto.UpsertTaskRequest;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@CacheConfig(cacheNames = "task")
public class TaskController {
    private final TaskService service;

    @GetMapping("/task")
    public ResponseEntity<Page<Map<String, Object>>> getTasks(
            @ModelAttribute TaskFilter filter,
            @RequestParam String fields) {
        return ResponseEntity.ok(service.findAll(fields, filter));
    }


    @GetMapping("/task/{id}")
    public ResponseEntity<Map<String, Object>> taskDetails(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/task/{id}")
    public ResponseEntity<Map<String, Object>> saveTask(
            @PathVariable String id,
            @ModelAttribute UpsertTaskRequest request,
            @ModelAttribute CustomFieldsAndLists customFieldsAndLists,
            @ModelAttribute TaskReferencesRequest referenceRequest) {
        return ResponseEntity.ok(service.saveTask(id, request, customFieldsAndLists, referenceRequest));
    }

    @PutMapping("/task")
    public ResponseEntity<Page<Map<String, Object>>> updateTask(
            @RequestParam(value = "taskId") String id,
            @RequestParam(value = "Start", required = false) LocalDate targetStart,
            @RequestParam(value = "End", required = false) LocalDate targetFinish) {
        return ResponseEntity.ok(service.updateTaskByStartAndFinish(id, targetStart, targetFinish));
    }

    @PostMapping("/task")
    public ResponseEntity<Map<String, Object>> createTask(
            @ModelAttribute UpsertTaskRequest request,
            @ModelAttribute CustomFieldsAndLists customFieldsAndLists,
            @ModelAttribute TaskReferencesRequest referenceRequest) {
        return new ResponseEntity<>(
                service.createNewTask(request, customFieldsAndLists, referenceRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/task/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTask(@PathVariable String id) {
        service.deleteTaskById(id);
    }
}
