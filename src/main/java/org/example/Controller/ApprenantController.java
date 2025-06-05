package org.example.Controller;

import org.example.DTO.ApprenantDTO;
import org.example.Model.Apprenant;
import org.example.Model.Groupe;
import org.example.Repository.ApprenantRepository;
import org.example.Repository.GroupeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/apprenants")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ApprenantController {

    @Autowired
    private ApprenantRepository apprenantRepository;

    @Autowired
    private GroupeRepository groupeRepository;

    @GetMapping
    public ResponseEntity<List<ApprenantDTO>> getAllApprenants() {
        List<ApprenantDTO> apprenants = apprenantRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(apprenants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprenantDTO> getApprenantById(@PathVariable Long id) {
        Apprenant apprenant = apprenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Apprenant not found"));
        return ResponseEntity.ok(convertToDTO(apprenant));
    }

    @GetMapping("/groupe/{id}")
    public ResponseEntity<List<ApprenantDTO>> getApprenantsByGroupeId(@PathVariable Long id) {
        // Find the specific group by id
        Optional<Groupe> groupe = groupeRepository.findById(id);
        if (!groupe.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Groupe not found");
        }

        // Get all students from this group
        List<Apprenant> apprenants = new ArrayList<>(groupe.get().getApprenants());

        // Convert to DTOs
        List<ApprenantDTO> apprenantDTOs = apprenants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(apprenantDTOs);
    }


    @PostMapping
    public ResponseEntity<ApprenantDTO> createApprenant(@RequestBody ApprenantDTO apprenantDTO) {
        Apprenant apprenant = convertToEntity(apprenantDTO);

        // Handle group assignments
        if (apprenantDTO.getGroupeIds() != null && !apprenantDTO.getGroupeIds().isEmpty()) {
            List<Groupe> groupes = groupeRepository.findAllById(apprenantDTO.getGroupeIds());
            for (Groupe groupe : groupes) {
                groupe.addApprenant(apprenant);
            }
        }

        Apprenant saved = apprenantRepository.save(apprenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprenantDTO> updateApprenant(@PathVariable Long id, @RequestBody ApprenantDTO apprenantDTO) {
        Apprenant existing = apprenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Apprenant not found"));

        existing.setNom(apprenantDTO.getNom());
        existing.setEmail(apprenantDTO.getEmail());
        existing.setPhone(apprenantDTO.getPhone());
        existing.setStatus(apprenantDTO.getStatus());
        existing.setJoinDate(apprenantDTO.getJoinDate());

        // Clear existing groups to prevent duplicates
        if (existing.getGroupes() != null) {
            for (Groupe groupe : new ArrayList<>(existing.getGroupes())) {
                groupe.getApprenants().remove(existing);
            }
            existing.getGroupes().clear();
        }

        // Add new group assignments
        if (apprenantDTO.getGroupeIds() != null && !apprenantDTO.getGroupeIds().isEmpty()) {
            List<Groupe> groupes = groupeRepository.findAllById(apprenantDTO.getGroupeIds());
            for (Groupe groupe : groupes) {
                groupe.addApprenant(existing);
            }
        }

        Apprenant updated = apprenantRepository.save(existing);

        // Make sure to save the groups as well to update the join table
        if (updated.getGroupes() != null) {
            groupeRepository.saveAll(updated.getGroupes());
        }

        return ResponseEntity.ok(convertToDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprenant(@PathVariable Long id) {
        if (!apprenantRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Apprenant not found");
        }

        // Get the apprenant to manage relationships before deletion
        Apprenant apprenant = apprenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Apprenant not found"));

        // Remove this apprenant from all groups
        if (apprenant.getGroupes() != null) {
            for (Groupe groupe : new ArrayList<>(apprenant.getGroupes())) {
                groupe.getApprenants().remove(apprenant);
                groupeRepository.save(groupe);
            }
            apprenant.getGroupes().clear();
        }

        apprenantRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ApprenantDTO convertToDTO(Apprenant apprenant) {
        ApprenantDTO dto = new ApprenantDTO();
        dto.setId(apprenant.getId());
        dto.setNom(apprenant.getNom());
        dto.setEmail(apprenant.getEmail());
        dto.setPhone(apprenant.getPhone());
        dto.setJoinDate(apprenant.getJoinDate());
        dto.setStatus(apprenant.getStatus());

        if (apprenant.getGroupes() != null) {
            dto.setGroupeIds(apprenant.getGroupes().stream().map(Groupe::getId).toList());
            dto.setGroupeNames(apprenant.getGroupes().stream().map(Groupe::getName).toList());
        }

        return dto;
    }


    private Apprenant convertToEntity(ApprenantDTO dto) {
        Apprenant apprenant = new Apprenant();
        apprenant.setId(dto.getId());
        apprenant.setNom(dto.getNom());
        apprenant.setEmail(dto.getEmail());
        apprenant.setPhone(dto.getPhone());
        apprenant.setStatus(dto.getStatus());
        apprenant.setJoinDate(dto.getJoinDate());

        // Initialize the groups list
        if (apprenant.getGroupes() == null) {
            apprenant.setGroupes(new ArrayList<>());
        }

        return apprenant;
    }
}