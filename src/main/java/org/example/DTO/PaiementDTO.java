package org.example.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PaiementDTO {
    private Long id;
    private int montant;
    private LocalDate date;
    private String statut;
    private Long apprenantId;

    // Add apprenant name field to avoid circular references
    private String apprenantNom;
}