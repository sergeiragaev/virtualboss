package net.virtualboss.common.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.model.enums.TaskStatus;
import net.virtualboss.common.util.TaskStatusConverter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.lang.NonNull;

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
public class Task implements Comparable<Task> {

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
    private Integer duration;

    @Column(nullable = false, name = "target_finish")
    private LocalDate targetFinish;

    @Column(name = "actual_finish")
    private LocalDate actualFinish;

    @Column(nullable = false)
    @JsonProperty("status")
    @Convert(converter = TaskStatusConverter.class)
    private TaskStatus status;

    @Column(name = "is_pending")
    @Builder.Default
    private Boolean isPending = false;

    @ManyToMany(cascade = {DETACH, MERGE, PERSIST, REFRESH})
    @JoinTable(
            name = "tasks_follows",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "follows_id"))
    private Set<Task> follows;

    @Column(name = "task_order")
    private String taskOrder;

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
    @Builder.Default
    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(nullable = false, name = "contact_id")
    private Contact contact;

    @ManyToOne
    @JoinColumn(name = "requested_id", referencedColumnName = "id")
    private Employee requested;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private Job job;

    @ManyToMany(cascade = {DETACH, MERGE, PERSIST, REFRESH})
    @JoinTable(
            name = "entity_custom_values",
            joinColumns = @JoinColumn(name = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "custom_value_id"))
    @Builder.Default
    private Set<FieldValue> customFieldsAndListsValues = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "group_members",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    private Set<Group> groups = new HashSet<>();

    @ManyToMany(cascade = {DETACH, MERGE, PERSIST, REFRESH})
    @JoinTable(name = "tasks_follows",
            joinColumns = @JoinColumn(name = "follows_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private Set<Task> pendingTasks = new HashSet<>();

    @Column(name = "finish_plus")
    @Builder.Default
    private Integer finishPlus = 1;

    public String getCustomValueByName(String name) {
        return customFieldsAndListsValues.stream()
                .filter(fieldValue -> fieldValue.getField().getName().equals(name))
                .findAny().orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("{0} does not have value!", name)
                )).getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "number=" + number +
                ", description='" + description + '\'' +
                ", start='" + targetStart + '\'' +
                ", duration='" + duration + '\'' +
                ", finish='" + targetFinish + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull Task other) {
        return number.compareTo(other.getNumber());
    }

    public void assignTasksToJobAndContact() {
        if (job != null) job.getTasks().add(this);
        this.getContact().getTasks().add(this);
    }
}
