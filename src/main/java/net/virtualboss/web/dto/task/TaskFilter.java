package net.virtualboss.web.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import net.virtualboss.model.enums.DateCriteria;
import net.virtualboss.model.enums.DateRange;
import net.virtualboss.model.enums.DateType;
import net.virtualboss.web.dto.filter.CommonFilter;

import java.time.LocalDate;
import java.util.List;

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
    private List<String> custIds;

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

    @JsonProperty("IsDeleted")
    private Boolean isDeleted;

}
