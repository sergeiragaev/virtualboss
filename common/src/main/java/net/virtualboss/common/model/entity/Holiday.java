package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "holidays",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"date", "country_code"},
                        name = "uk_holidays_date_country"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false, columnDefinition = "DATE")
    private LocalDate date;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_recurring", nullable = false)
    private boolean isRecurring = true;

    // equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holiday holiday = (Holiday) o;
        return Objects.equals(id, holiday.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Holiday{" +
               "id=" + id +
               ", date=" + date +
               ", countryCode='" + countryCode + '\'' +
               ", name='" + name + '\'' +
               ", isRecurring=" + isRecurring +
               '}';
    }
}