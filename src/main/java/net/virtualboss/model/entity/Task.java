package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
@ToString
public class Task  implements Serializable {

    @Id
    @GeneratedValue
    private UUID id;

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
    private boolean isDeleted;

    @ManyToOne
    @JoinColumn(nullable = false, name = "contact_id", referencedColumnName = "id")
    private Contact contact;

    @ManyToOne
    @JoinColumn(name = "requested_id", referencedColumnName = "id")
    private Employee requested;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private Job job;

}
