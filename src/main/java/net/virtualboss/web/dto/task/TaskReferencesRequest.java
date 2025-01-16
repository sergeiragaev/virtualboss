package net.virtualboss.web.dto.task;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskReferencesRequest {
    private String jobNumber;
    private String contactId;
    private String requested;
    private String groups;
    private String pending;
}
