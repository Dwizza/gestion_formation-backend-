package org.example.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Groupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Renamed from "nom"
    private int capaciteMax;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status; // active, completed, pending

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "formation_id")
    @JsonManagedReference
    private Formation formation;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Formateur trainer;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "groupe_apprenants",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "apprenant_id")
    )

    private List<Apprenant> apprenants = new ArrayList<>();

    public void addApprenant(Apprenant apprenant) {
        if (apprenants == null) {
            apprenants = new ArrayList<>();
        }
        apprenants.add(apprenant);
        apprenant.getGroupes().add(this);
    }


    // Optional helper method to expose learner count
    public int getLearnerCount() {
        return apprenants != null ? apprenants.size() : 0;
    }
    @ManyToMany(mappedBy = "groupes")
    @JsonIgnore
    private List<Formateur> formateurs = new ArrayList<>();
}
