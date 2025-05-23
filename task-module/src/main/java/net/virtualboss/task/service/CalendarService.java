package net.virtualboss.task.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.task.web.dto.CalendarDto;
import net.virtualboss.common.model.entity.Task;
import net.virtualboss.common.repository.ContactRepository;
import net.virtualboss.common.repository.JobRepository;
import net.virtualboss.common.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class CalendarService {
    private final ContactRepository contactRepository;
    private final TaskRepository taskRepository;
    private final JobRepository jobRepository;

    public List<CalendarDto> findAll() {
        List<Task> taskList = taskRepository.findAll();

        taskList.sort(Comparator.comparing(Task::getTargetStart));

        List<CalendarDto> calendarDtoList = new ArrayList<>();
        for (Task task : taskList) {
            CalendarDto calendarDto = CalendarDto.builder()
                    .start(task.getTargetStart())
                    .end(task.getTargetFinish())
                    .title(task.getDescription())
                    .job(getJobNumber(task))
                    .color("#FF0000")
                    .person(task.getContact().getPerson())
                    .duration(String.valueOf(task.getDuration()))
                    .taskNotes(task.getNotes())
                    .url("api/v1/task/" + task.getId())
                    .taskId(task.getId().toString())
                    .build();
            calendarDtoList.add(calendarDto);
        }
        return calendarDtoList;
    }

    private String getJobNumber(Task task) {
        if (task.getJob() == null) return "";
        return task.getJob().getNumber();
    }
}
