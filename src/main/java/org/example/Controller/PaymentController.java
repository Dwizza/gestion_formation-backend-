package org.example.Controller;

import org.example.DTO.UnpaidPaymentsReportDTO;
import org.example.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}

