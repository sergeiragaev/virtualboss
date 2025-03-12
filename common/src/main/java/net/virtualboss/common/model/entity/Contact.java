package net.virtualboss.common.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.virtualboss.common.exception.EntityNotFoundException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.CascadeType.REFRESH;

@Entity
@Table(name = "contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    @Id
    @GeneratedValue
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    private String company;
    private String profession;

    public String getPerson() {
        return firstName + " " + lastName +
                " (" + company + ")";
    }

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String supervisor;
    private String spouse;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "web_site")
    private String webSite;

    @Column(name = "workers_comp_date")
    private LocalDate workersCompDate;

    @Column(name = "insurance_date")
    private LocalDate insuranceDate;

    private String comments;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT", name = "notes_rtf")
    private String notesRtf;

    private String fax;

    private String email;

    private String phones;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(cascade = DETACH, mappedBy = "contact")
    @Builder.Default
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany(cascade = {DETACH, MERGE, PERSIST, REFRESH})
    @JoinTable(
            name = "entity_custom_values",
            joinColumns = @JoinColumn(name = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "custom_value_id"))
    private Set<FieldValue> customFieldsAndListsValues;

    @ManyToMany
    @JoinTable(name = "group_members",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    private Set<Group> groups = new HashSet<>();

    public String getCustomValueByName(String name) {
        return customFieldsAndListsValues.stream()
                .filter(fieldValue -> fieldValue.getField().getName().equals(name))
                .findAny().orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Custom field with name {0} does not exist", name)
                )).getValue();
    }

}
