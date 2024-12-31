package net.virtualboss.web.dto.task;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class UpsertTaskRequest {
    private UUID id;
    private String description;
    private LocalDate targetStart;
    private Short duration;
    private LocalDate targetFinish;
    private LocalDate actualFinish;
    private String status;
    private String order;
    private String notes;
    private Boolean marked;
    private String jobNumber;
    private String contactId;
    private String requested;
    private Boolean isDeleted;
    private String groups;

    private Map<String, String> CustomFieldsAndLists;
}
