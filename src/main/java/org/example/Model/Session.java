package org.example.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String status; // PLANNED, COMPLETED, CANCELLED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "formateur_id")
    private Formateur formateur;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Presence> presences = new ArrayList<>();

    // Helper method to add presence records
    public void addPresence(Presence presence) {
        presences.add(presence);
        presence.setSession(this);
    }

    // Helper method to generate presence records for all learners in the group
    public void generatePresenceRecords() {
        if (this.groupe != null && this.groupe.getApprenants() != null) {
            for (Apprenant apprenant : this.groupe.getApprenants()) {
                Presence presence = new Presence();
                presence.setApprenant(apprenant);
                presence.setSession(this);
                presence.setDate(this.date);
                presence.setStatut("ABSENT"); // Default status
                this.presences.add(presence);
            }
        }
    }
}