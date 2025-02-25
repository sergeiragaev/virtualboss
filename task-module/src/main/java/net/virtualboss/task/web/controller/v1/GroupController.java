package net.virtualboss.task.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.service.GroupService;
import net.virtualboss.common.web.dto.GroupDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService service;

    @GetMapping("/taskGroup")
    public ResponseEntity<List<GroupDto>> findAllTaskGroups(
            @RequestParam(required = false, name = "Id") String groupIds) {
        return ResponseEntity.ok(service.findAll(EntityType.TASK, groupIds));
    }

    @PostMapping("/taskGroup")
    public ResponseEntity<GroupDto> createTaskGroup(
            @RequestParam String groupName) {
        return ResponseEntity.ok(service.create(EntityType.TASK, groupName));
    }

}
