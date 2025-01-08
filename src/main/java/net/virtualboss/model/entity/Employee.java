package net.virtualboss.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.model.enums.RoleType;

import java.util.*;

@Entity
@Table(name = "employees")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Employee {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String email;

    private String password;

    private Integer color;

    @Column(columnDefinition = "TEXT")
    @ToString.Exclude
    private String notes;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    @OneToMany(cascade = CascadeType.DETACH)
    @JsonIgnore
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();
}
