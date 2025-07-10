package org.example.DTO;

import java.util.List;

public class UnpaidPaymentsReportDTO {

    private double totalUnpaidAmount;
    private long numberOfUnpaidLearners;
    private double averageOverdueMonths;
    private List<PaiementDTO> unpaidPayments;

    public UnpaidPaymentsReportDTO(double totalUnpaidAmount, long numberOfUnpaidLearners, double averageOverdueMonths, List<PaiementDTO> unpaidPayments) {
        this.totalUnpaidAmount = totalUnpaidAmount;
        this.numberOfUnpaidLearners = numberOfUnpaidLearners;
        this.averageOverdueMonths = averageOverdueMonths;
        this.unpaidPayments = unpaidPayments;
    }

    // Getters and setters
    public double getTotalUnpaidAmount() {
        return totalUnpaidAmount;
    }

    public void setTotalUnpaidAmount(double totalUnpaidAmount) {
        this.totalUnpaidAmount = totalUnpaidAmount;
    }

    public long getNumberOfUnpaidLearners() {
        return numberOfUnpaidLearners;
    }

    public void setNumberOfUnpaidLearners(long numberOfUnpaidLearners) {
        this.numberOfUnpaidLearners = numberOfUnpaidLearners;
    }

    public double getAverageOverdueMonths() {
        return averageOverdueMonths;
    }

    public void setAverageOverdueMonths(double averageOverdueMonths) {
        this.averageOverdueMonths = averageOverdueMonths;
    }

    public List<PaiementDTO> getUnpaidPayments() {
        return unpaidPayments;
    }

    public void setUnpaidPayments(List<PaiementDTO> unpaidPayments) {
        this.unpaidPayments = unpaidPayments;
    }
}

