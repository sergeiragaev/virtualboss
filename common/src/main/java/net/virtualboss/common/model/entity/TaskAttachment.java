package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_attachments")
@Setter
@Getter
public class TaskAttachment extends BaseAttachment {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
}