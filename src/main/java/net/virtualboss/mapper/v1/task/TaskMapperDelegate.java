package net.virtualboss.mapper.v1.task;

import net.virtualboss.model.entity.Task;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.EmployeeService;
import net.virtualboss.service.GroupService;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TaskMapperDelegate implements TaskMapperV1 {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private MainService mainService;
    @Autowired
    private GroupService groupService;

    @Override
    public Task addCFLAndGroupsToTask(Task task, CustomFieldsAndLists customFieldsAndLists, String contactId, String jobNumber, String requested, String taskGroups) {
        task.setContact(mainService.getContactById(contactId));
        task.setJob(mainService.getJobByNumber(jobNumber));
        task.setRequested(employeeService.findByName(requested));
        task.setCustomFieldsAndListsValues(mainService.createCustomList(customFieldsAndLists, "Task"));
        task.setGroups(groupService.getGroups(EntityType.TASK, taskGroups));
        return task;
    }
}
