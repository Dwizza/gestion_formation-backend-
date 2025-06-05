package org.example.Controller;

import org.example.Model.Formation;
import org.example.Model.Groupe;
import org.example.Model.Formateur;
import org.example.Repository.FormationRepository;
import org.example.Repository.GroupeRepository;
import org.example.Repository.FormateurRepository;
import org.example.DTO.GroupeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groupes")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class GroupeController {

    @Autowired
    private GroupeRepository groupeRepository;

    @Autowired
    private FormationRepository formationRepository;

    @Autowired
    private FormateurRepository formateurRepository;

    // GET all groups
    @GetMapping
    public ResponseEntity<List<GroupeDTO>> getAllGroupes() {
        List<GroupeDTO> dtos = groupeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // GET by id
    @GetMapping("/{id}")
    public ResponseEntity<GroupeDTO> getById(@PathVariable Long id) {
        Groupe g = groupeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(convertToDTO(g));
    }

    // POST create
    @PostMapping
    public ResponseEntity<GroupeDTO> create(@RequestBody GroupeDTO dto) {
        if (dto.getName() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Formation f = formationRepository.findById(dto.getFormationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formation not found"));

        Groupe g = new Groupe();
        g.setName(dto.getName());
        g.setCapaciteMax(dto.getCapaciteMax());
        g.setStartDate(dto.getStartDate());
        g.setEndDate(dto.getEndDate());
        g.setStatus(dto.getStatus());
        g.setFormation(f);

        // **NEW**: assign trainer if provided
        if (dto.getTrainerId() != null) {
            Formateur t = formateurRepository.findById(dto.getTrainerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trainer not found"));
            g.setTrainer(t);
        }

        Groupe saved = groupeRepository.save(g);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    // PUT update
    @PutMapping("/{id}")
    public ResponseEntity<GroupeDTO> update(@PathVariable Long id, @RequestBody GroupeDTO dto) {
        Groupe g = groupeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        g.setName(dto.getName());
        g.setCapaciteMax(dto.getCapaciteMax());
        g.setStartDate(dto.getStartDate());
        g.setEndDate(dto.getEndDate());
        g.setStatus(dto.getStatus());

        if (dto.getFormationId() != null) {
            Formation f = formationRepository.findById(dto.getFormationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formation not found"));
            g.setFormation(f);
        }

        // **NEW**: update trainer
        if (dto.getTrainerId() != null) {
            Formateur t = formateurRepository.findById(dto.getTrainerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trainer not found"));
            g.setTrainer(t);
        } else {
            g.setTrainer(null);
        }

        Groupe updated = groupeRepository.save(g);
        return ResponseEntity.ok(convertToDTO(updated));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!groupeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        groupeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/formateur/{formateurId}")
    public ResponseEntity<List<GroupeDTO>> getGroupesByFormateur(@PathVariable Long formateurId) {
        // Check if the formateur exists
        Formateur formateur = formateurRepository.findById(formateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formateur not found"));

        // Find groups where this formateur is assigned as trainer
        List<Groupe> groupes = groupeRepository.findByTrainerId(formateurId);

        // Convert to DTOs
        List<GroupeDTO> dtos = groupes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Convert entity ➔ DTO
    private GroupeDTO convertToDTO(Groupe g) {
        GroupeDTO dto = new GroupeDTO();
        dto.setId(g.getId());
        dto.setName(g.getName());
        dto.setCapaciteMax(g.getCapaciteMax());
        dto.setStartDate(g.getStartDate());
        dto.setEndDate(g.getEndDate());
        dto.setStatus(g.getStatus());
        dto.setLearnerCount(g.getLearnerCount());

        if (g.getFormation() != null) {
            dto.setFormationId(g.getFormation().getId());
            dto.setFormationTitle(g.getFormation().getTitle());
        }

        // **Emit** trainer info
        if (g.getTrainer() != null) {
            dto.setTrainerId(g.getTrainer().getId());
            dto.setTrainerName(g.getTrainer().getName());
        }

        return dto;
    }
}
