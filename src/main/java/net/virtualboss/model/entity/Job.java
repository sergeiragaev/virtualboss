package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.exception.EntityNotFoundException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;

import static jakarta.persistence.CascadeType.*;

@Entity
@Table(name = "jobs")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Job {
    @Id
    @GeneratedValue
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @Column(nullable = false)
    private String number;

    private String lot;

    private String subdivision;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "lock_box")
    private String lockBox;

    @Column(columnDefinition = "TEXT")
    private String directions;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT", name = "notes_rtf")
    private String notesRtf;

    private String address1;

    private String address2;

    private String city;

    private String state;

    private String postal;

    @Column(name = "home_phone")
    private String homePhone;

    @Column(name = "work_phone")
    private String workPhone;

    @Column(name = "cell_phone")
    private String cellPhone;

    private String fax;

    private String company;

    private String email;

    private String country;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(cascade = DETACH)
    @Builder.Default
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
