package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "contacts")
@Data
public class Contact {
    @Id
    @GeneratedValue
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    private String company;
    private String profession;

    public String getPerson() {
        return firstName + " " + lastName +
                " (" + company + ")";
    }

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String supervisor;
    private String spouse;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "web_site")
    private String webSite;

    @Column(name = "workers_comp_date")
    private LocalDate workersCompDate;

    @Column(name = "insurance_date")
    private LocalDate insuranceDate;

    private String comments;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT", name = "notes_rtf")
    private String notesRtf;

    private String fax;

    private String email;

    private String phones;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @OneToMany(cascade = CascadeType.DETACH)
    public Set<Task> tasks = new HashSet<>();

}
