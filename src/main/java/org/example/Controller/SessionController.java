package org.example.Controller;

import lombok.RequiredArgsConstructor;
import org.example.Model.Apprenant;
import org.example.Model.Groupe;
import org.example.Model.Presence;
import org.example.Model.Session;
import org.example.Repository.GroupeRepository;
import org.example.Repository.PresenceRepository;
import org.example.Repository.SessionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SessionController {

    private final SessionRepository sessionRepository;
    private final GroupeRepository groupeRepository;
    private final PresenceRepository presenceRepository;

    // ✅ Create session with    auto-generated presence records
    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody Session session) {
        try {
            if (session.getGroupe() == null || session.getGroupe().getId() == null) {
                return ResponseEntity.badRequest().body("Groupe ID is required.");
            }

            Optional<Groupe> groupeOpt = groupeRepository.findById(session.getGroupe().getId());
            if (groupeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Groupe not found.");
            }

            Groupe groupe = groupeOpt.get();

            // Auto-assign trainer if not set explicitly
            session.setFormateur(groupe.getTrainer());
            session.setGroupe(groupe);
            session.setStatus("PLANNED");

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

            // Reload the session with presence
            Session fullSession = sessionRepository.findById(savedSession.getId()).orElse(savedSession);
            return new ResponseEntity<>(fullSession, HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create session: " + e.getMessage());
        }
    }
}
