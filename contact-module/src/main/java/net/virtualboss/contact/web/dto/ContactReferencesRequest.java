package net.virtualboss.contact.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactReferencesRequest {
    private String groups;
    private String company;
    private String profession;
    private String addresses;
    private String phones;
}
