package org.example.Controller;

import lombok.RequiredArgsConstructor;
import org.example.DTO.SessionDTO;
import org.example.Model.Apprenant;
import org.example.Model.Formateur;
import org.example.Model.Groupe;
import org.example.Model.Presence;
import org.example.Model.Session;
import org.example.Repository.FormateurRepository;
import org.example.Repository.GroupeRepository;
import org.example.Repository.PresenceRepository;
import org.example.Repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SessionController {

    private final SessionRepository sessionRepository;
    private final GroupeRepository groupeRepository;
    private final PresenceRepository presenceRepository;
    private final FormateurRepository formateurRepository;

    // ✅ Create session with auto-generated presence records
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createSession(@RequestBody SessionDTO sessionDTO) {
        try {
            if (sessionDTO.getGroupeId() == null) {
                return ResponseEntity.badRequest().body("Groupe ID is required.");
            }

            Optional<Groupe> groupeOpt = groupeRepository.findById(sessionDTO.getGroupeId());
            if (groupeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Groupe not found.");
            }

            Optional<Formateur> formateurOpt = formateurRepository.findById(sessionDTO.getFormateurId());
            if (formateurOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Formateur not found.");
            }

            Groupe groupe = groupeOpt.get();
            Formateur formateur = formateurOpt.get();

            // Convert DTO to Entity
            Session session = new Session();
            session.setTitle(sessionDTO.getTitle());
            session.setDescription(sessionDTO.getDescription());
            session.setDate(sessionDTO.getDate());
            session.setStartTime(sessionDTO.getStartTime());
            session.setEndTime(sessionDTO.getEndTime());
            session.setDays(sessionDTO.getDays() != null ? sessionDTO.getDays() : new ArrayList<>());
            session.setStatus(sessionDTO.getStatus() != null ? sessionDTO.getStatus() : "PLANNED");
            session.setGroupe(groupe);
            session.setFormateur(formateur);

            // Save session first
            Session savedSession = sessionRepository.save(session);

            // Create presence records for each learner in the group
            List<Presence> presenceList = new ArrayList<>();
            for (Apprenant apprenant : groupe.getApprenants()) {
                Presence presence = new Presence();
                presence.setSession(savedSession);
                presence.setApprenant(apprenant);
                presence.setGroupe(groupe);
                presence.setDate(session.getDate());
                presence.setStatut("ABSENT");
                presenceList.add(presence);
            }

            presenceRepository.saveAll(presenceList);

            return new ResponseEntity<>(convertToDTO(savedSession), HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create session: " + e.getMessage());
        }
    }

    // ✅ Get all sessions with groups and trainers data
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSessionsWithData() {
        try {
            List<Session> sessions = sessionRepository.findAll();
            List<SessionDTO> sessionDTOs = sessions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // Get all groupes data for select dropdown
            List<Groupe> groupes = groupeRepository.findAll();
            List<Map<String, Object>> groupeData = groupes.stream().map(groupe -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", groupe.getId());
                data.put("name", groupe.getName());
                data.put("trainerName", groupe.getTrainer().getName());
                return data;
            }).collect(Collectors.toList());

            // Get all formateurs data for select dropdown
            List<Formateur> formateurs = formateurRepository.findAll();
            List<Map<String, Object>> formateurData = formateurs.stream().map(formateur -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", formateur.getId());
                data.put("name", formateur.getName());
                data.put("email", formateur.getEmail());
                return data;
            }).collect(Collectors.toList());

            // Combine all data in one response
            Map<String, Object> response = new HashMap<>();
            response.put("sessions", sessionDTOs);
            response.put("groupes", groupeData);
            response.put("formateurs", formateurData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Get session by ID
    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable Long id) {
        try {
            Optional<Session> sessionOpt = sessionRepository.findById(id);
            if (sessionOpt.isPresent()) {
                return ResponseEntity.ok(convertToDTO(sessionOpt.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Update session
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Long id, @RequestBody SessionDTO sessionDTO) {
        try {
            Optional<Session> sessionOpt = sessionRepository.findById(id);
            if (sessionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Session session = sessionOpt.get();

            // Update fields
            if (sessionDTO.getTitle() != null) session.setTitle(sessionDTO.getTitle());
            if (sessionDTO.getDescription() != null) session.setDescription(sessionDTO.getDescription());
            if (sessionDTO.getDate() != null) session.setDate(sessionDTO.getDate());
            if (sessionDTO.getStartTime() != null) session.setStartTime(sessionDTO.getStartTime());
            if (sessionDTO.getEndTime() != null) session.setEndTime(sessionDTO.getEndTime());
            if (sessionDTO.getDays() != null) session.setDays(sessionDTO.getDays());
            if (sessionDTO.getStatus() != null) session.setStatus(sessionDTO.getStatus());

            // Update groupe if provided
            if (sessionDTO.getGroupeId() != null) {
                Optional<Groupe> groupeOpt = groupeRepository.findById(sessionDTO.getGroupeId());
                if (groupeOpt.isPresent()) {
                    session.setGroupe(groupeOpt.get());
                }
            }

            // Update formateur if provided
            if (sessionDTO.getFormateurId() != null) {
                Optional<Formateur> formateurOpt = formateurRepository.findById(sessionDTO.getFormateurId());
                if (formateurOpt.isPresent()) {
                    session.setFormateur(formateurOpt.get());
                }
            }

            Session updatedSession = sessionRepository.save(session);
            return ResponseEntity.ok(convertToDTO(updatedSession));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update session: " + e.getMessage());
        }
    }

    // ✅ Delete session
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        try {
            Optional<Session> sessionOpt = sessionRepository.findById(id);
            if (sessionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            sessionRepository.deleteById(id);
            return ResponseEntity.ok().body("Session deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete session: " + e.getMessage());
        }
    }

    // ✅ Helper method to convert Session to SessionDTO
    private SessionDTO convertToDTO(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setDescription(session.getDescription());
        dto.setDate(session.getDate());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setDays(session.getDays());
        dto.setStatus(session.getStatus());

        if (session.getGroupe() != null) {
            dto.setGroupeId(session.getGroupe().getId());
            dto.setGroupeName(session.getGroupe().getName());
        }

        if (session.getFormateur() != null) {
            dto.setFormateurId(session.getFormateur().getId());
            dto.setFormateurName(session.getFormateur().getName());
        }

        return dto;
    }
}
