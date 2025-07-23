package org.example.Service;

import org.example.DTO.PaiementDTO;
import org.example.DTO.UnpaidPaymentsReportDTO;
import org.example.Model.Paiement;
import org.example.Model.StatutPaiement;
import org.example.Repository.PaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaiementRepository paiementRepository;

    public UnpaidPaymentsReportDTO getUnpaidPaymentsReport() {
        List<Paiement> unpaidPayments = paiementRepository.findByStatut(StatutPaiement.UNPAID);

        double totalUnpaidAmount = unpaidPayments.stream()
                .mapToDouble(Paiement::getMontant)
                .sum();

        long numberOfUnpaidLearners = unpaidPayments.stream()
                .map(Paiement::getApprenant)
                .distinct()
                .count();

        double averageOverdueMonths = unpaidPayments.stream()
                .mapToLong(p -> ChronoUnit.MONTHS.between(p.getDate(), LocalDate.now()))
                .average()
                .orElse(0.0);

        List<PaiementDTO> unpaidPaymentsDTO = unpaidPayments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new UnpaidPaymentsReportDTO(totalUnpaidAmount, numberOfUnpaidLearners, averageOverdueMonths, unpaidPaymentsDTO);
    }

    public PaiementDTO markAsPaid(Long paymentId) {
        Paiement paiement = paiementRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        paiement.setStatut(StatutPaiement.PAID);
        Paiement updatedPaiement = paiementRepository.save(paiement);
        return convertToDTO(updatedPaiement);
    }

    private PaiementDTO convertToDTO(Paiement paiement) {
        PaiementDTO paiementDTO = new PaiementDTO();
        paiementDTO.setId(paiement.getId());
        paiementDTO.setApprenantId(paiement.getApprenant().getId());
        paiementDTO.setApprenantNom(paiement.getApprenant().getNom());
        paiementDTO.setMontant(paiement.getMontant());
        paiementDTO.setDate(paiement.getDate());
        paiementDTO.setStatut(paiement.getStatut().name());
        return paiementDTO;
    }
}
