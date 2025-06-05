package org.example.Repository;

import org.example.Model.Apprenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@RepositoryRestResource
public interface ApprenantRepository extends JpaRepository<Apprenant, Long> {
    List<Apprenant> findByemail(String email);
    @Query("SELECT a FROM Apprenant a LEFT JOIN a.paiements p " +
            "WHERE p IS NULL OR p.statut <> org.example.Model.StatutPaiement.PAID")
    //List<Apprenant> findApprenantsWithUnpaidPaiements();
   // List<Apprenant> findByGroupesId(Long groupeId);
    List<Apprenant> findByGroupeId(Long id);






}

