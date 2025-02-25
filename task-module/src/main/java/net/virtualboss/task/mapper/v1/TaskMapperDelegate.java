package net.virtualboss.task.mapper.v1;

import net.virtualboss.common.model.entity.Task;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.task.service.EmployeeService;
import net.virtualboss.common.service.GroupService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
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
