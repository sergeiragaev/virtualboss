package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_attachments")
@Setter
@Getter
public class TaskAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Column(name = "is_clip")
    private boolean isClip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
}