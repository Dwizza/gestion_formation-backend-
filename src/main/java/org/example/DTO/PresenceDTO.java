package org.example.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PresenceDTO {
    private Long id;
    private Long apprenantId;
    private String apprenantNom;
    private Long groupeId;
    private String groupeName;
    private LocalDate date;
    private String statut;
}
