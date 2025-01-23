package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.exception.CircularLinkingException;
import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.model.enums.TaskStatus;
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
    @Enumerated(EnumType.STRING)
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
    @Builder.Default
    private Boolean isDeleted = false;

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
    @JoinTable(name = "tasks_children",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "child_id")
    )
    @Builder.Default
    private Set<Task> children = new HashSet<>();

    @ManyToMany(cascade = {DETACH, MERGE, PERSIST, REFRESH})
    @JoinTable(name = "tasks_follows",
            joinColumns = @JoinColumn(name = "follows_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private Set<Task> pendingTasks;

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


    public void calculateDates(Task task) {
        if (!task.getFollows().isEmpty()) calculateStart(task);
        calculateFinish(task);
        if (task.getStatus() == TaskStatus.Active) task.setActualFinish(null);

        addChildren(task);

        if (task.getPendingTasks() != null) {
            for (Task current : task.getPendingTasks()) {
                calculateDates(current);
            }
        } else {
            for (Task current : task.getChildren()) {
                calculateDates(current);
            }
        }
    }

    private void calculateStart(Task task) {
        LocalDate start = LocalDate.ofEpochDay(0);
        int shift = 1;
        for (Task parentTask : task.getFollows()) {
            LocalDate parentTaskFinish = parentTask.getStatus() == TaskStatus.Active ?
                    parentTask.getTargetFinish() : parentTask.getActualFinish();
            start = start.isBefore(parentTaskFinish) ? parentTaskFinish : start;
            shift = task.getFinishPlus();
        }
        start = getValidDate(shift, start.plusDays(1));
        task.setTargetStart(start);
    }

    private void calculateFinish(Task task) {
        LocalDate finish = getValidDate(task.getDuration(), task.getTargetStart());
        task.setTargetFinish(finish);
    }

    public static void addChildren(Task task) {
        for (Task parentTask : task.getFollows()) {
            parentTask.getChildren().add(task);
            parentTask.getChildren().addAll(task.getChildren());
            addChildren(parentTask);
        }
    }

    private LocalDate getValidDate(int shift, LocalDate date) {
        date = date.minusDays(1);
        if (shift == 0) {
            return getValidDate(1, date);
        } else {
            int days = 0;
            do {
                date = shift < 0 ? date.minusDays(1) : date.plusDays(1);
                int dow = date.getDayOfWeek().getValue();
                if (!(dow == 6 || dow == 7)) {
                    days = shift < 0 ? --days : ++days;
                }
            } while (shift != days);
        }
        return date;
    }

    public Set<Task> removeChildrenFromParents(Task task, Set<Task> recalculatedTasks, Set<Task> childrenTasks) {
        for (Task parentTask : task.getFollows()) {
            childrenTasks.add(task);
            childrenTasks.addAll(task.getChildren());
            recalculatedTasks.add(parentTask);
            parentTask.getChildren().removeAll(childrenTasks);
        }
        for (Task parentTask : task.getFollows()) {
            return removeChildrenFromParents(parentTask, recalculatedTasks, childrenTasks);
        }

        return recalculatedTasks;
    }

    public static void checkIfFollowsAlreadyPending(Task task, Task taskFromDb) {
        task.getFollows().forEach(parent -> {
            if (taskFromDb.getChildren().contains(parent)) throw new CircularLinkingException(
                    MessageFormat.format(
                            "Cannot make {0} pending {1}, " +
                                    "because {1} follows {0}",
                            taskFromDb, parent)
            );
        });
    }

    public void assignTasksToJobAndContact() {
        if (job != null) job.getTasks().add(this);
        this.getContact().getTasks().add(this);
    }

    public static void recalculate(Task task) {
        task.calculateDates(task);
    }

    public void setTargetStart(LocalDate targetStart) {
        this.targetStart = getValidDate(1, targetStart);
        this.targetFinish = getValidDate(this.duration, this.targetStart);
    }
}
