package org.example.Repository;

import org.example.Model.Formateur;
import org.example.Model.Groupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupeRepository extends JpaRepository<Groupe, Long> {

    // Find groups by formation ID
    @Query("SELECT g FROM Groupe g WHERE g.formation.id = :formationId")
    List<Groupe> findByFormationId(@Param("formationId") Long formationId);
    List<Groupe> findByTrainer(Formateur trainer);

    @Query("SELECT g FROM Groupe g WHERE g.trainer.id = :formateurId OR :formateurId IN (SELECT f.id FROM g.formateurs f)")
    List<Groupe> findGroupsByFormateurId(@Param("formateurId") Long formateurId);


    // Find groups by trainer ID
    @Query("SELECT g FROM Groupe g WHERE g.trainer.id = :trainerId")
    List<Groupe> findByTrainerId(@Param("trainerId") Long trainerId);

    // Find groups with formation eagerly loaded
    @Query("SELECT g FROM Groupe g JOIN FETCH g.formation")
    List<Groupe> findAllWithFormation();
}