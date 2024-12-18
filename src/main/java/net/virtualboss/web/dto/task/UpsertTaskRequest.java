package net.virtualboss.web.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
}
