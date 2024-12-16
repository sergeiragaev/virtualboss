package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Job  implements Serializable {
    @Id
    @GeneratedValue
    private UUID id;

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
    private boolean isDeleted;

    @OneToMany(cascade = CascadeType.DETACH)
    private List<Task> tasks = new ArrayList<>();

}
