package org.example.DTO;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupeDTO {
    private Long id;
    private String name;
    private int capaciteMax;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private int learnerCount;

    // Formation data directly included
    private Long formationId;
    private String formationTitle;

    // **Trainer** fields
    private Long trainerId;
    private String trainerName;
}
