package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.mapper.v1.ContactMapperV1;
import net.virtualboss.web.dto.CalendarDto;
import net.virtualboss.model.entity.Task;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.repository.JobRepository;
import net.virtualboss.repository.TaskRepository;
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
    private final ContactMapperV1 mapper;

    public List<CalendarDto> findAll() {
        List<Task> taskList = taskRepository.findAll();

        taskList.sort(Comparator.comparing(Task::getTargetStart));

        List<CalendarDto> calendarDtoList = new ArrayList<>();
        for (Task task : taskList) {
            CalendarDto calendarDto = CalendarDto.builder()
                    .start(task.getTargetStart())
                    .end(task.getTargetFinish())
                    .title(task.getDescription())
                    .job(task.getJob().getNumber())
                    .color("#FF0000")
                    .person(task.getContact().getPerson())
                    .duration(String.valueOf(task.getDuration()))
                    .taskNotes(task.getNotes())
                    .url("TaskDetail?TaskId=" + task.getId())
                    .build();
            calendarDtoList.add(calendarDto);
        }
        return calendarDtoList;
    }
}
