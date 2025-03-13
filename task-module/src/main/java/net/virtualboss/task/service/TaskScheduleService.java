package net.virtualboss.task.service;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.model.entity.Task;
import net.virtualboss.common.model.enums.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskScheduleService {
    private final WorkingDaysCalculator workingDaysCalculator;

    @Transactional
    public void recalculateTaskDates(Task task) {

        if (!task.getFollows().isEmpty()) {
            calculateStartDate(task);
        }
        calculateFinishDate(task);

        if (task.getPendingTasks() != null) task.getPendingTasks().forEach(this::recalculateTaskDates);
    }

    private void calculateStartDate(Task task) {
        LocalDate latestFinish = task.getFollows().stream()
                .map(t -> t.getStatus() == TaskStatus.ACTIVE ?
                        t.getTargetFinish() : t.getActualFinish())
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new EntityNotFoundException("No valid parent tasks found"));

        LocalDate newStart = workingDaysCalculator.addWorkDays(latestFinish, task.getFinishPlus(), "US");
        task.setTargetStart(newStart);
    }

    private void calculateFinishDate(Task task) {
        LocalDate newFinish = workingDaysCalculator.addWorkDays(
                task.getTargetStart(),
                task.getDuration() - 1,
                "US"
        );
        task.setTargetFinish(newFinish);
    }
}