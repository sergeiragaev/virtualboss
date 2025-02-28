package net.virtualboss.task.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("IsActive")
    private Boolean isActive;
    @JsonProperty("IsDone")
    private Boolean isDone;
    @JsonProperty("IsMarked")
    private Boolean isMarked;

    @JsonProperty("TaskGroup")
    private String taskGroup;

    @JsonProperty("JobIds")
    private List<String> jobIds;
    @JsonProperty("CustIds")
    private List<String> contactIds;

    @JsonProperty("TaskIds")
    private List<String> taskIds;

    @JsonProperty("TaskGroupIds")
    private List<String> taskGroupIds;
    @JsonProperty("JobGroupIds")
    private List<String> jobGroupIds;
    @JsonProperty("ContactGroupIds")
    private List<String> contactGroupIds;

    @JsonProperty("IsDateRange")
    private Boolean isDateRange;

    @JsonProperty("DateFrom")
    private LocalDate dateFrom;
    @JsonProperty("DateTo")
    private LocalDate dateTo;

    @JsonProperty("DateType")
    private Integer dateType = DateType.TARGET_START.getValue();
    @JsonProperty("DateRange")
    private Integer dateRange = DateRange.TODAY.getValue();
    @JsonProperty("DateCriteria")
    private Integer dateCriteria = DateCriteria.ON_OR_BEFORE.getValue();

    @JsonProperty("ThisDate")
    private LocalDate thisDate;

    @JsonProperty("LinkingTask")
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
