package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.virtualboss.common.model.enums.TaskStatus;

@Entity
@Table(name = "task_status_color")
@Getter
@Setter
public class TaskStatusColor {
    @Id
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    private String color;
}
