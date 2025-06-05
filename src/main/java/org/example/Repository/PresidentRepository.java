package org.example.Repository;

import org.example.Model.President;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PresidentRepository extends JpaRepository<President, Long> {
    Optional<President> findByEmail(String email);
}