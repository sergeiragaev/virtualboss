package net.virtualboss.contact.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UpsertContactRequest {
    private UUID id;
    private String firstName;
    private String lastName;
    private String supervisor;
    private String spouse;
    private String taxId;
    private String webSite;
    private LocalDate workersCompDate;
    private LocalDate insuranceDate;
    private String comments;
    private String notes;
    private String fax;
    private String email;
    private String company;
    private String profession;
    private String groups;
}
