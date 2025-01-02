package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.exception.EntityNotFoundException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static jakarta.persistence.CascadeType.*;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    private Long number;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, name = "target_start")
    private LocalDate targetStart;

    @Column(nullable = false)
    private Short duration;

    @Column(nullable = false, name = "target_finish")
    private LocalDate targetFinish;

    @Column(name = "actual_finish")
    private LocalDate actualFinish;

    @Column(nullable = false)
    private String status;

    @OneToMany
    @Builder.Default
    private List<Task> follows = new ArrayList<>();

    @Column(name = "\"order\"")
    private String order;

    @Column(columnDefinition = "TEXT")
    @ToString.Exclude
    private String notes;

    @Column(columnDefinition = "TEXT", name = "notes_rtf")
    @ToString.Exclude
    private String notesRtf;

    @Column(columnDefinition = "TEXT")
    @ToString.Exclude
    private String files;

    private Boolean marked;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(nullable = false, name = "contact_id", referencedColumnName = "id")
    private Contact contact;

    @ManyToOne
    @JoinColumn(name = "requested_id", referencedColumnName = "id")
    private Employee requested;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private Job job;

    @ManyToMany(cascade = {DETACH, MERGE, PERSIST, REFRESH})
    @JoinTable(
            name = "task_custom_values",
            joinColumns = @JoinColumn(name = "task_id"),
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

    private void setCustomValueByName(String name, String value) {
        customFieldsAndListsValues.stream()
                .filter(fieldValue -> fieldValue.getField().getName().equals(name))
                .findAny().orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Custom field with name {0} does not exist", name)
                )).setValue(value);
    }

    public void setCustomFieldsAndListsValues(Set<FieldValue> values) {
        for (FieldValue value : values) {
            setCustomValueByName(value.getField().getName(), value.getValue());
        }
    }
}
