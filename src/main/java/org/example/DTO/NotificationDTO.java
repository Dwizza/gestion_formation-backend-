package org.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String titre;
    private String message;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLecture;
    private boolean lu;
    private String type;
    private Long apprenantId;
    private String apprenantNom;
    private boolean urgente;
}
