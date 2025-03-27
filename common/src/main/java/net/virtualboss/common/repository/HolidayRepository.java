package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.Holiday;
import net.virtualboss.common.repository.projection.HolidayProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    @Query(value = """
                SELECT date, name 
                FROM get_holidays(:year, :countryCode)
                UNION
                SELECT date, name 
                FROM holidays 
                WHERE EXTRACT(YEAR FROM date) = :year 
                  AND (country_code = :countryCode OR country_code IS NULL)
                  AND is_recurring = false
            """, nativeQuery = true)
    List<HolidayProjection> findHolidays(@Param("year") int year,
                                         @Param("countryCode") String countryCode);
}