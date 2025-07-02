package org.example.Controller;

import lombok.RequiredArgsConstructor;
import org.example.DTO.PresenceDTO;
import org.example.Model.Apprenant;
import org.example.Model.Groupe;
import org.example.Model.Presence;
import org.example.Repository.ApprenantRepository;
import org.example.Repository.GroupeRepository;
import org.example.Repository.PresenceRepository;
import org.example.Service.NotificationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/presences")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PresenceController {

    private final PresenceRepository presenceRepository;
    private final ApprenantRepository apprenantRepository;
    private final GroupeRepository groupeRepository;
    private final NotificationService notificationService;

    @GetMapping
    public List<PresenceDTO> getAll() {
        return presenceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Admin view: Filter presences by multiple criteria
    @GetMapping("/filter")
    public ResponseEntity<List<PresenceDTO>> filterPresences(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long apprenantId,
            @RequestParam(required = false) Long groupeId) {

        List<Presence> presences;

        if (date != null && apprenantId != null && groupeId != null) {
            // Filter by all three criteria
            presences = presenceRepository.findByDateAndApprenantIdAndGroupeId(date, apprenantId, groupeId);
        } else if (date != null && apprenantId != null) {
            // Filter by date and student
            presences = presenceRepository.findByDateAndApprenantId(date, apprenantId);
        } else if (date != null && groupeId != null) {
            // Filter by date and group
            presences = presenceRepository.findByDateAndGroupeId(date, groupeId);
        } else if (apprenantId != null && groupeId != null) {
            // Filter by student and group
            presences = presenceRepository.findByApprenantIdAndGroupeId(apprenantId, groupeId);
        } else if (date != null) {
            // Filter by date only
            presences = presenceRepository.findByDate(date);
        } else if (apprenantId != null) {
            // Filter by student only
            presences = presenceRepository.findByApprenantId(apprenantId);
        } else if (groupeId != null) {
            // Filter by group only
            presences = presenceRepository.findByGroupeId(groupeId);
        } else {
            // No filters, get all
            presences = presenceRepository.findAll();
        }

        List<PresenceDTO> dtos = presences.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/byTrainer")
    public List<Presence> getByTrainerGroupeAndDate(
            @RequestParam String trainerName,
            @RequestParam Long groupeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return presenceRepository.findByTrainerNameAndDateAndGroupeId(trainerName, date, groupeId);
    }


    // Get summary statistics for admin dashboard
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long groupeId) {

        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1); // Default: last month
        }

        if (endDate == null) {
            endDate = LocalDate.now(); // Default: today
        }

        List<Presence> presences;
        if (groupeId != null) {
            presences = presenceRepository.findByDateBetweenAndGroupeId(startDate, endDate, groupeId);
        } else {
            presences = presenceRepository.findByDateBetween(startDate, endDate);
        }

        long totalPresent = presences.stream().filter(p -> "PRESENT".equals(p.getStatut())).count();
        long totalAbsent = presences.stream().filter(p -> "ABSENT".equals(p.getStatut())).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", presences.size());
        summary.put("present", totalPresent);
        summary.put("absent", totalAbsent);
        summary.put("presentRate", presences.isEmpty() ? 0 : (totalPresent * 100.0 / presences.size()));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<PresenceDTO>> getPresencesByGroupeAndDate(
            @PathVariable Long groupeId,
            @RequestParam("date") String dateStr) {

        // Convertir la date string → LocalDate

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }

        List<Presence> presences = presenceRepository.findByGroupeIdAndDate(groupeId, date);
        List<PresenceDTO> dtos = presences.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PresenceDTO> getById(@PathVariable Long id) {
        return presenceRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PresenceDTO dto) {
        try {
            System.out.println("DEBUG: Création d'une présence pour l'apprenant ID: " + dto.getApprenantId() +
                    ", groupe ID: " + dto.getGroupeId() +
                    ", statut: " + dto.getStatut());

            Optional<Apprenant> apprenantOpt = apprenantRepository.findById(dto.getApprenantId());
            Optional<Groupe> groupeOpt = groupeRepository.findById(dto.getGroupeId());

            if (apprenantOpt.isEmpty()) {
                System.out.println("ERREUR: Apprenant non trouvé avec ID " + dto.getApprenantId());
                return ResponseEntity.badRequest().body("Apprenant non trouvé avec ID " + dto.getApprenantId());
            }

            if (groupeOpt.isEmpty()) {
                System.out.println("ERREUR: Groupe non trouvé avec ID " + dto.getGroupeId());
                return ResponseEntity.badRequest().body("Groupe non trouvé avec ID " + dto.getGroupeId());
            }

            Presence presence = new Presence();
            presence.setApprenant(apprenantOpt.get());
            presence.setGroupe(groupeOpt.get());
            presence.setDate(dto.getDate());
            presence.setStatut(dto.getStatut());

            System.out.println("DEBUG: Sauvegarde de la présence dans la base de données");
            Presence saved = presenceRepository.save(presence);

            // Créer une notification si l'apprenant est absent
            if ("ABSENT".equals(saved.getStatut())) {
                System.out.println("DEBUG: Création d'une notification pour absence");
                try {
                    notificationService.createAbsenceNotification(saved);
                    System.out.println("DEBUG: Notification créée avec succès");
                } catch (Exception e) {
                    System.err.println("ERREUR lors de la création de la notification d'absence: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("DEBUG: Présence enregistrée avec succès, ID: " + saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
        } catch (Exception e) {
            System.err.println("ERREUR lors de la création de la présence: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PresenceDTO dto) {
        try {
            System.out.println("DEBUG: Mise à jour de la présence ID: " + id +
                    ", statut: " + dto.getStatut());

            Optional<Presence> presenceOpt = presenceRepository.findById(id);

            if (presenceOpt.isEmpty()) {
                System.out.println("ERREUR: Présence non trouvée avec ID " + id);
                return ResponseEntity.notFound().build();
            }

            Optional<Apprenant> apprenantOpt = apprenantRepository.findById(dto.getApprenantId());
            Optional<Groupe> groupeOpt = groupeRepository.findById(dto.getGroupeId());

            if (apprenantOpt.isEmpty() || groupeOpt.isEmpty()) {
                System.out.println("ERREUR: Apprenant ou Groupe non trouvé");
                return ResponseEntity.badRequest().body("Apprenant ou Groupe non trouvé");
            }

            Presence presence = presenceOpt.get();
            String ancienStatut = presence.getStatut();

            presence.setApprenant(apprenantOpt.get());
            presence.setGroupe(groupeOpt.get());
            presence.setDate(dto.getDate());
            presence.setStatut(dto.getStatut());

            System.out.println("DEBUG: Sauvegarde des modifications dans la base de données");
            Presence updated = presenceRepository.save(presence);

            // Créer une notification si le statut passe à "ABSENT"
            if (!"ABSENT".equals(ancienStatut) && "ABSENT".equals(updated.getStatut())) {
                System.out.println("DEBUG: Création d'une notification pour changement de statut vers ABSENT");
                try {
                    notificationService.createAbsenceNotification(updated);
                    System.out.println("DEBUG: Notification créée avec succès");
                } catch (Exception e) {
                    System.err.println("ERREUR lors de la création de la notification d'absence: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("DEBUG: Présence mise à jour avec succès");
            return ResponseEntity.ok(convertToDTO(updated));
        } catch (Exception e) {
            System.err.println("ERREUR lors de la mise à jour de la présence: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue: " + e.getMessage());
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createBulk(@RequestBody List<PresenceDTO> presenceDTOs) {
        try {
            System.out.println("DEBUG: Création en masse de " + presenceDTOs.size() + " présences");

            List<Presence> presencesToSave = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (PresenceDTO dto : presenceDTOs) {
                if (dto.getApprenantId() == null || dto.getGroupeId() == null || dto.getDate() == null) {
                    errors.add("Apprenant ID, Groupe ID, et Date ne doivent pas être null.");
                    continue;
                }

                Optional<Apprenant> apprenantOpt = apprenantRepository.findById(dto.getApprenantId());
                Optional<Groupe> groupeOpt = groupeRepository.findById(dto.getGroupeId());

                if (apprenantOpt.isEmpty() || groupeOpt.isEmpty()) {
                    errors.add("ID Apprenant ou Groupe invalide: " + dto.getApprenantId() + " / " + dto.getGroupeId());
                    continue;
                }

                Presence presence = new Presence();
                presence.setApprenant(apprenantOpt.get());
                presence.setGroupe(groupeOpt.get());
                presence.setDate(dto.getDate());
                presence.setStatut(dto.getStatut());

                presencesToSave.add(presence);
                System.out.println("DEBUG: Préparation de la présence pour l'apprenant " +
                        apprenantOpt.get().getNom() + " avec statut " + dto.getStatut());
            }

            if (!errors.isEmpty()) {
                System.out.println("ERREUR: Des erreurs sont survenues lors de la création en masse: " + errors);
                return ResponseEntity.badRequest().body(errors);
            }

            System.out.println("DEBUG: Sauvegarde de " + presencesToSave.size() + " présences dans la base de données");
            List<Presence> savedPresences = presenceRepository.saveAll(presencesToSave);

            // Créer des notifications pour les absents
            int notificationsCreated = 0;
            for (Presence absence : savedPresences) {
                if ("ABSENT".equals(absence.getStatut())) {
                    try {
                        notificationService.createAbsenceNotification(absence);
                        notificationsCreated++;
                    } catch (Exception e) {
                        System.err.println("ERREUR lors de la création de la notification d'absence: " + e.getMessage());
                    }
                }
            }

            System.out.println("DEBUG: " + savedPresences.size() + " présences enregistrées avec succès. " +
                    notificationsCreated + " notifications créées.");
            return ResponseEntity.ok("Présences enregistrées avec succès: " + savedPresences.size() +
                    ", Notifications créées: " + notificationsCreated);
        } catch (Exception e) {
            System.err.println("ERREUR lors de la création en masse des présences: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!presenceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        presenceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private PresenceDTO convertToDTO(Presence presence) {
        PresenceDTO dto = new PresenceDTO();
        dto.setId(presence.getId());
        dto.setDate(presence.getDate());
        dto.setStatut(presence.getStatut());

        if (presence.getApprenant() != null) {
            dto.setApprenantId(presence.getApprenant().getId());
            dto.setApprenantNom(presence.getApprenant().getNom());
        }

        if (presence.getGroupe() != null) {
            dto.setGroupeId(presence.getGroupe().getId());
            dto.setGroupeName(presence.getGroupe().getName());
        }

        return dto;
    }
}