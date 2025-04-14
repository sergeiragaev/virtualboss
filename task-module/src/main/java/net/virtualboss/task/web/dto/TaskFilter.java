package net.virtualboss.task.web.dto;

import lombok.Getter;
import lombok.Setter;
import net.virtualboss.common.model.enums.DateCriteria;
import net.virtualboss.common.model.enums.DateRange;
import net.virtualboss.common.model.enums.DateType;
import net.virtualboss.common.web.dto.filter.CommonFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class TaskFilter extends CommonFilter {
    private Boolean isActive;
    private Boolean isDone;
    private Boolean isMarked;
    private String taskGroup;

    private List<String> jobIds;
    private List<String> contactIds;
    private List<String> taskIds;

    private List<String> taskGroupIds;
    private List<String> jobGroupIds;
    private List<String> contactGroupIds;

    private Boolean isDateRange;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private Integer dateType = DateType.TARGET_START.getValue();
    private Integer dateRange = DateRange.TODAY.getValue();
    private Integer dateCriteria = DateCriteria.ON_OR_BEFORE.getValue();
    private LocalDate thisDate;

    private String linkingTask;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TaskFilter that = (TaskFilter) o;
        return Objects.equals(isActive, that.isActive) && Objects.equals(isDone, that.isDone) && Objects.equals(isMarked, that.isMarked) && Objects.equals(taskGroup, that.taskGroup) && Objects.equals(jobIds, that.jobIds) && Objects.equals(contactIds, that.contactIds) && Objects.equals(taskIds, that.taskIds) && Objects.equals(taskGroupIds, that.taskGroupIds) && Objects.equals(jobGroupIds, that.jobGroupIds) && Objects.equals(contactGroupIds, that.contactGroupIds) && Objects.equals(isDateRange, that.isDateRange) && Objects.equals(dateFrom, that.dateFrom) && Objects.equals(dateTo, that.dateTo) && Objects.equals(dateType, that.dateType) && Objects.equals(dateRange, that.dateRange) && Objects.equals(dateCriteria, that.dateCriteria) && Objects.equals(thisDate, that.thisDate) && Objects.equals(linkingTask, that.linkingTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isMarked, taskGroup, jobIds, contactIds, taskIds, taskGroupIds, jobGroupIds, contactGroupIds, isDateRange, dateFrom, dateTo, dateType, dateRange, dateCriteria, thisDate, linkingTask);
    }
}
