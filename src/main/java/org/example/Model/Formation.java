package org.example.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;          // frontend: title
    private String description;    // frontend: description
    private String duration;       // frontend: duration, e.g. "8 weeks"
    private Double price;          // frontend: price
    private String status;         // active | inactive | upcoming

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Groupe> groupes;
}
