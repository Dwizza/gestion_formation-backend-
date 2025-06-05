package org.example.Repository;



import org.example.Model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // Find sessions by group ID
    List<Session> findByGroupeId(Long groupeId);

    // Find sessions by trainer ID
    List<Session> findByFormateurId(Long formateurId);

    // Find sessions by trainer ID and date range
    List<Session> findByFormateurIdAndDateBetween(Long formateurId, LocalDate startDate, LocalDate endDate);

    // Find sessions by group ID and date range
    List<Session> findByGroupeIdAndDateBetween(Long groupeId, LocalDate startDate, LocalDate endDate);

    // Find sessions by date
    List<Session> findByDate(LocalDate date);

    // Find sessions occurring in the future
    @Query("SELECT s FROM Session s WHERE s.date >= :currentDate ORDER BY s.date ASC")
    List<Session> findUpcomingSessions(@Param("currentDate") LocalDate currentDate);

    // Find sessions for a trainer within a month
    @Query("SELECT s FROM Session s WHERE s.formateur.id = :formateurId AND YEAR(s.date) = :year AND MONTH(s.date) = :month")
    List<Session> findByFormateurAndYearAndMonth(
            @Param("formateurId") Long formateurId,
            @Param("year") int year,
            @Param("month") int month);
}
