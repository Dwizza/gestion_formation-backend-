package org.example.DTO;

import org.example.Model.Groupe;
import org.example.Model.Session;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class GroupeEmploiDailyDTO {
    private Long id;
    private String name;
    private int capaciteMax;
    private String status;
    private Map<LocalDate, List<Session>> emploiDuTempsDaily;

    public GroupeEmploiDailyDTO(Groupe groupe, Map<LocalDate, List<Session>> emploiDuTempsDaily) {
        this.id = groupe.getId();
        this.name = groupe.getName();
        this.capaciteMax = groupe.getCapaciteMax();
        this.status = groupe.getStatus();
        this.emploiDuTempsDaily = emploiDuTempsDaily;
    }
}

