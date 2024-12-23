package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
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

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

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
