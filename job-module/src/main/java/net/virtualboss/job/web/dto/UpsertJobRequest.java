package net.virtualboss.job.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpsertJobRequest {
    private UUID id;
    private String number;
    private String lot;
    private String subdivision;
    private String ownerId;
    private String lockBox;
    private String directions;
    private String notes;
    private String email;
    private String groups;
}
