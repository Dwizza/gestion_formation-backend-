package org.example.Controller;

import org.example.DTO.PaiementDTO;
import org.example.DTO.UnpaidPaymentsReportDTO;
import org.example.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/unpaid-report")
    public ResponseEntity<UnpaidPaymentsReportDTO> getUnpaidPaymentsReport() {
        UnpaidPaymentsReportDTO report = paymentService.getUnpaidPaymentsReport();
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{paymentId}/mark-as-paid")
    public ResponseEntity<?> markAsPaid(@PathVariable String paymentId) {
        try {
            Long id = Long.parseLong(paymentId);
            PaiementDTO updatedPayment = paymentService.markAsPaid(id);
            return ResponseEntity.ok(updatedPayment);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid payment ID format");
        }
    }
}
