package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.common.model.enums.EntityType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    @Id
    @GeneratedValue
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @Enumerated(EnumType.STRING)
    private EntityType type;

    private String name;

    private String description;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

}
