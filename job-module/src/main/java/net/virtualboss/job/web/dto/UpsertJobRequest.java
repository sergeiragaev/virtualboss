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
    private String ownerName;
    private String lockBox;
    private String directions;
    private String notes;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String postal;
    private String homePhone;
    private String workPhone;
    private String cellPhone;
    private String fax;
    private String company;
    private String email;
    private String country;
    private String groups;
}
