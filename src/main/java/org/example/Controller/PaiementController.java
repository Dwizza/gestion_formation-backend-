package org.example.Controller;

import lombok.RequiredArgsConstructor;
import org.example.Model.Apprenant;
import org.example.Model.Paiement;
import org.example.DTO.PaiementDTO;
import org.example.Repository.ApprenantRepository;
import org.example.Repository.PaiementRepository;
import org.example.Service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.Model.StatutPaiement;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PaiementController {

    private final PaiementRepository paiementRepository;
    private final ApprenantRepository apprenantRepository;
    private final NotificationService notificationService;

    @GetMapping
    public List<PaiementDTO> getAll() {
        return paiementRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO> getById(@PathVariable Long id) {
        return paiementRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PaiementDTO> create(@RequestBody PaiementDTO dto) {
        Optional<Apprenant> apprenantOpt = apprenantRepository.findById(dto.getApprenantId());
        if (apprenantOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Paiement paiement = new Paiement();
        paiement.setMontant(dto.getMontant());
        paiement.setDate(dto.getDate());
        paiement.setStatut(StatutPaiement.valueOf(dto.getStatut()));
        paiement.setApprenant(apprenantOpt.get());

        Paiement saved = paiementRepository.save(paiement);

        // Envoi d'une notification si le paiement a une date d'échéance proche ou dépassée
        if (dto.getDate() != null) {
            long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), dto.getDate());
            if (joursRestants <= 7) { // Si l'échéance est dans moins de 7 jours ou déjà dépassée
                notificationService.createPaymentNotification(saved, (int) joursRestants);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaiementDTO> update(@PathVariable Long id, @RequestBody PaiementDTO dto) {
        Optional<Paiement> paiementOpt = paiementRepository.findById(id);
        Optional<Apprenant> apprenantOpt = apprenantRepository.findById(dto.getApprenantId());

        if (paiementOpt.isEmpty() || apprenantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Paiement paiement = paiementOpt.get();
        paiement.setMontant(dto.getMontant());
        paiement.setDate(dto.getDate());
        paiement.setStatut(StatutPaiement.valueOf(dto.getStatut()));
        paiement.setApprenant(apprenantOpt.get());

        Paiement updated = paiementRepository.save(paiement);

        // Vérifier si le statut du paiement a été modifié
        if (paiement.getStatut() != StatutPaiement.PAID && paiement.getDate() != null) {
            long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), paiement.getDate());
            if (joursRestants <= 7) {
                notificationService.createPaymentNotification(updated, (int) joursRestants);
            }
        }

        return ResponseEntity.ok(convertToDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!paiementRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        paiementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Nouveau endpoint pour vérifier et envoyer des notifications pour les paiements en attente
    @PostMapping("/verifier-echeances")
    public ResponseEntity<?> verifierEcheances() {
        try {
            System.out.println("DEBUG: Vérification des échéances de paiement");

            // Récupérer tous les paiements qui ne sont pas encore payés
            List<Paiement> paiementsEnAttente = paiementRepository.findByStatut(StatutPaiement.PENDING);
            List<Paiement> paiementsEnRetard = paiementRepository.findByStatut(StatutPaiement.UNPAID);

            // Combiner les deux listes
            List<Paiement> paiementsNonPayes = new ArrayList<>(paiementsEnAttente);
            paiementsNonPayes.addAll(paiementsEnRetard);

            System.out.println("DEBUG: " + paiementsNonPayes.size() + " paiements non payés trouvés");

            int notificationsEnvoyees = 0;

            for (Paiement paiement : paiementsNonPayes) {
                if (paiement.getDate() != null) {
                    long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), paiement.getDate());
                    System.out.println("DEBUG: Paiement ID " + paiement.getId() + " - jours restants: " + joursRestants);

                    // Envoyer une notification si l'échéance est dans moins de 7 jours ou déjà dépassée
                    if (joursRestants <= 7) {
                        notificationService.createPaymentNotification(paiement, (int) joursRestants);
                        notificationsEnvoyees++;
                        System.out.println("DEBUG: Notification créée pour le paiement ID " + paiement.getId());
                    }
                }
            }

            System.out.println("DEBUG: Total des notifications envoyées: " + notificationsEnvoyees);
            return ResponseEntity.ok("Notifications envoyées : " + notificationsEnvoyees);
        } catch (Exception e) {
            System.err.println("ERREUR lors de la vérification des échéances: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la vérification des échéances: " + e.getMessage());
        }
    }

    private PaiementDTO convertToDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setDate(paiement.getDate());
        if (paiement.getStatut() != null) {
            dto.setStatut(paiement.getStatut().name());
        }

        if (paiement.getApprenant() != null) {
            dto.setApprenantId(paiement.getApprenant().getId());
            // Add additional fields if needed, like apprenant name
            dto.setApprenantNom(paiement.getApprenant().getNom());
        }

        return dto;
    }
}