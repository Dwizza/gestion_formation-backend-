package org.example.Repository;

import org.example.Model.Formateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormateurRepository extends JpaRepository<Formateur, Long> {
    // Basic CRUD methods are provided by JpaRepository

    // You can add additional query methods as needed
    Formateur findByEmail(String email);
}