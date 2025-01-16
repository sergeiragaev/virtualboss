package net.virtualboss.mapper.v1.task;

import net.virtualboss.model.entity.Task;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.EmployeeService;
import net.virtualboss.service.GroupService;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.task.TaskReferencesRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TaskMapperDelegate implements TaskMapperV1 {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private MainService mainService;
    @Autowired
    private GroupService groupService;

    @Override
    public Task setCFLAndReferencesToTask(Task task, CustomFieldsAndLists customFieldsAndLists, TaskReferencesRequest request) {
        task.setCustomFieldsAndListsValues(mainService.createCustomList(customFieldsAndLists, EntityType.TASK));
        task.setContact(mainService.getContactById(request.getContactId()));
        task.setJob(mainService.getJobByNumber(request.getJobNumber()));
        task.setRequested(employeeService.findByName(request.getRequested()));
        task.setGroups(groupService.getGroups(EntityType.TASK, request.getGroups()));
        task.setFollows(mainService.createFollows(request.getPending()));
        return task;
    }
}
