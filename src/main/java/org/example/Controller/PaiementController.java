package org.example.Controller;

import lombok.RequiredArgsConstructor;
import org.example.Model.Apprenant;
import org.example.Model.Paiement;
import org.example.DTO.PaiementDTO;
import org.example.Repository.ApprenantRepository;
import org.example.Repository.PaiementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.Model.StatutPaiement;
import org.springframework.web.server.ResponseStatusException;

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

    private PaiementDTO convertToDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setDate(paiement.getDate());
        dto.setStatut(paiement.getStatut().name());

        if (paiement.getApprenant() != null) {
            dto.setApprenantId(paiement.getApprenant().getId());
            // Add additional fields if needed, like apprenant name
            dto.setApprenantNom(paiement.getApprenant().getNom());
        }

        return dto;
    }
}