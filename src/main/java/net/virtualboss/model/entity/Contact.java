package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Data
public class Contact implements Serializable {
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
    public List<Task> tasks = new ArrayList<>();

    @Column(name = "custom_field1")
    private String customField1;
    @Column(name = "custom_field2")
    private String customField2;
    @Column(name = "custom_field3")
    private String customField3;
    @Column(name = "custom_field4")
    private String customField4;
    @Column(name = "custom_field5")
    private String customField5;
    @Column(name = "custom_field6")
    private String customField6;

    @Column(name = "custom_list1")
    private String customList1;
    @Column(name = "custom_list2")
    private String customList2;
    @Column(name = "custom_list3")
    private String customList3;
    @Column(name = "custom_list4")
    private String customList4;
    @Column(name = "custom_list5")
    private String customList5;
    @Column(name = "custom_list6")
    private String customList6;

}
