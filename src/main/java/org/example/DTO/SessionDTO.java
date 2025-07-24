package org.example.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SessionDTO {
    private Long id;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private List<String> days; // Nouveaux jours de la semaine
    private String status;
    private Long groupeId;
    private String groupeName; // Pour afficher le nom du groupe
    private Long formateurId;
    private String formateurName; // Pour afficher le nom du formateur
}
