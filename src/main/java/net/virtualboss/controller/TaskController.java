package net.virtualboss.controller;

import lombok.RequiredArgsConstructor;
import net.virtualboss.model.dto.GroupDto;
import net.virtualboss.model.dto.TaskDto;
import net.virtualboss.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService service;

    @GetMapping("/taskfeed")
    public ResponseEntity<List<TaskDto>> taskFeed() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/taskdata")
    @ResponseStatus(HttpStatus.OK)
    public void taskData(@RequestParam boolean logincheck) {
    }

    @GetMapping("/TaskData")
    public ResponseEntity<TaskDto[]> taskDetails(@RequestParam String TaskId) {
        return ResponseEntity.ok(service.findById(TaskId));
    }

    @GetMapping("/TaskGroupData")
    public ResponseEntity<GroupDto[]> groupDetails() {
        return ResponseEntity.ok(new GroupDto[]{GroupDto.builder().build()});
    }
}
