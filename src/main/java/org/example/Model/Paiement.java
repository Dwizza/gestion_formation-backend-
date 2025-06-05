package org.example.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int montant;
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut; // e.g., PAID, UNPAID, PENDING

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "apprenant_id")
    @JsonBackReference  // Changed from JsonManagedReference to JsonBackReference to prevent circular reference
    private Apprenant apprenant;
}