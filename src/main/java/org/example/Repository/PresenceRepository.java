package org.example.Repository;

import org.example.Model.Apprenant;
import org.example.Model.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;

@RepositoryRestResource
public interface PresenceRepository extends JpaRepository<Presence, Long> {
    List<Presence> findByGroupeIdAndDate(Long groupeId, LocalDate date);

    // Added for admin filtering
    List<Presence> findByDate(LocalDate date);
    List<Presence> findByApprenantId(Long apprenantId);
    List<Presence> findByGroupeId(Long groupeId);
    List<Presence> findByDateAndApprenantId(LocalDate date, Long apprenantId);
    @Query("SELECT p FROM Presence p WHERE p.groupe.trainer.name = :trainerName AND p.date = :date AND p.groupe.id = :groupeId")
    List<Presence> findByTrainerNameAndDateAndGroupeId(@Param("trainerName") String trainerName, @Param("date") LocalDate date, @Param("groupeId") Long groupeId);

    List<Presence> findByDateAndGroupeId(LocalDate date, Long groupeId);
    List<Presence> findByApprenantIdAndGroupeId(Long apprenantId, Long groupeId);
    List<Presence> findByDateAndApprenantIdAndGroupeId(LocalDate date, Long apprenantId, Long groupeId);

    // For summary statistics
    List<Presence> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Presence> findByDateBetweenAndGroupeId(LocalDate startDate, LocalDate endDate, Long groupeId);
}
