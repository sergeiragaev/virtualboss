package net.virtualboss.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TaskFilterDto {
    @JsonProperty("IsActive")
    private Boolean isActive;
    @JsonProperty("IsDone")
    private Boolean isDone;
    @JsonProperty("IsMarked")
    private Boolean isMarked;

    @JsonProperty("FindString")
    private String findString;
    @JsonProperty("MatchType")
    private Integer matchType;
    @JsonProperty("Field")
    private String field;

    @JsonProperty("TaskGroup")
    private String taskGroup;

    @JsonProperty("JobIds")
    private List<String> jobIds;
    @JsonProperty("CustIds")
    private List<String> custIds;

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
    private Integer dateType = 1;
    @JsonProperty("DateRange")
    private Integer dateRange = 1;
    @JsonProperty("DateCriteria")
    private Integer dateCriteria = 1;

    @JsonProperty("ThisDate")
    private LocalDate thisDate;
}
