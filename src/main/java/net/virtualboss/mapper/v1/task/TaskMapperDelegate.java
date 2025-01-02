package net.virtualboss.mapper.v1.task;

import net.virtualboss.model.entity.Task;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.EmployeeService;
import net.virtualboss.service.GroupService;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TaskMapperDelegate implements TaskMapperV1 {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private MainService mainService;
    @Autowired
    private GroupService groupService;

    @Override
    public Task requestToTask(UpsertTaskRequest request, CustomFieldsAndLists customFieldsAndLists) {
        return Task.builder()
                .status(request.getStatus())
                .contact(mainService.getContactById(request.getContactId()))
                .job(mainService.getJobByNumber(request.getJobNumber()))
                .requested(employeeService.findByName(request.getRequested()))
                .description(request.getDescription())
                .notes(request.getNotes())
                .order(request.getOrder())
                .duration(request.getDuration())
                .targetStart(request.getTargetStart())
                .targetFinish(request.getTargetFinish())
                .actualFinish(request.getActualFinish())
                .marked(request.getMarked())
                .isDeleted(request.getIsDeleted() != null && request.getIsDeleted())
                .customFieldsAndListsValues(mainService.createCustomList(customFieldsAndLists, "Task"))
                .groups(groupService.getGroups(EntityType.TASK, request.getGroups()))
                .build();
    }
}
