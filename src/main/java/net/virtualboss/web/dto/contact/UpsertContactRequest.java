package net.virtualboss.web.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpsertContactRequest {
    private UUID id;
    private String company;
    private String profession;
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
    private String phones;
}
