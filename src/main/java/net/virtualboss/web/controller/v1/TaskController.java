package net.virtualboss.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.web.dto.GroupDto;
import net.virtualboss.web.dto.TaskDto;
import net.virtualboss.web.dto.TaskFilterDto;
import net.virtualboss.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService service;

    @GetMapping("/task")
    public ResponseEntity<List<Map<String, Object>>> getTasks(
            TaskFilterDto filter,
            @RequestParam(required = false) String fields,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "1") Integer page) {
        return ResponseEntity.ok(service.findAll(fields, filter, size, page));
    }

    @GetMapping("/taskdata")
    @ResponseStatus(HttpStatus.OK)
    public void taskData(@RequestParam boolean logincheck) {
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<TaskDto[]> taskDetails(@PathVariable String taskId) {
        return ResponseEntity.ok(service.findById(taskId));
    }

    @PutMapping("/task")
    public ResponseEntity<TaskDto[]> saveTask(TaskDto taskDto) {
        return ResponseEntity.ok(service.saveTask(taskDto));
    }


    @GetMapping("/TaskGroupData")
    public ResponseEntity<GroupDto[]> groupDetails() {
        return ResponseEntity.ok(new GroupDto[]{GroupDto.builder().build()});
    }
}
