package org.example.Repository;

import org.example.Model.Apprenant;
import org.example.Model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.example.Model.StatutPaiement;


public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Custom query to find paiements by month and year
    @Query("SELECT p FROM Paiement p WHERE FUNCTION('MONTH', p.date) = :month AND FUNCTION('YEAR', p.date) = :year")
    List<Paiement> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Find paiements by apprenant
    List<Paiement> findByApprenant(Apprenant apprenant);

    // Find unpaid paiements
    List<Paiement> findByStatut(StatutPaiement statut);

    // Custom query to find unpaid paiements
    @Query("SELECT p FROM Paiement p WHERE p.statut = :statut")
    List<Paiement> findUnpaidPaiements(@Param("statut") StatutPaiement statut);


}
