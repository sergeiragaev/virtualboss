package net.virtualboss.mapper.v1;

import net.virtualboss.model.entity.Task;
import net.virtualboss.service.EmployeeService;
import net.virtualboss.service.TaskService;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TaskMapperDelegate implements TaskMapperV1 {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private TaskService taskService;

    @Override
    public Task requestToTask(UpsertTaskRequest request) {
        return Task.builder()
                .status(request.getStatus())
                .contact(taskService.getContactById(request.getContactId()))
                .job(taskService.getJobByNumber(request.getJobNumber()))
                .requested(employeeService.findByName(request.getRequested()))
                .description(request.getDescription())
                .notes(request.getNotes())
                .order(request.getOrder())
                .duration(request.getDuration())
                .targetStart(request.getTargetStart())
                .targetFinish(request.getTargetFinish())
                .actualFinish(request.getActualFinish())
                .marked(request.getMarked())
                .build();
    }
}
