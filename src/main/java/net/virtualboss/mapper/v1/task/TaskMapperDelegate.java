package net.virtualboss.mapper.v1.task;

import net.virtualboss.model.entity.Task;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.EmployeeService;
import net.virtualboss.service.GroupService;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.task.TaskReferencesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class TaskMapperDelegate implements TaskMapperV1 {
    private EmployeeService employeeService;
    private MainService mainService;
    private GroupService groupService;

    @Autowired
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Autowired
    public void setMainService(MainService mainService) {
        this.mainService = mainService;
    }

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
