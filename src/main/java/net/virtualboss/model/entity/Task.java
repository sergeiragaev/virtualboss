package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    private Long number;

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
