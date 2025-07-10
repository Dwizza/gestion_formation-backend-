package org.example.config;

import org.example.Model.Paiement;
import org.example.Model.StatutPaiement;
import org.example.Repository.PaiementRepository;
import org.example.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
public class ScheduledTasks {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Daily check of payment deadlines
     * Executed every day at 9:00 AM
     */
    @Scheduled(cron = "0 0 9  * * ?")
    public void checkPaymentDeadlines() {
        System.out.println("Executing automatic check of payment deadlines");

        // Get all payments that are not yet paid
        // Use statuses other than "PAID": "PENDING" and "UNPAID"
        List<Paiement> pendingPayments = paiementRepository.findByStatut(StatutPaiement.PENDING);
        List<Paiement> overduePayments = paiementRepository.findByStatut(StatutPaiement.UNPAID);

        // Combine the two lists
        List<Paiement> unpaidPayments = pendingPayments;
        unpaidPayments.addAll(overduePayments);

        int notificationsSent = 0;

        for (Paiement paiement : unpaidPayments) {
            if (paiement.getDate() != null) {
                long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), paiement.getDate());

                // Send a notification for deadlines at:
                // - 7 days
                // - 3 days
                // - 1 day
                // - The same day
                // - Every day of delay
                if (daysRemaining <= 7 && (daysRemaining == 7 || daysRemaining == 3 || daysRemaining == 1 || daysRemaining <= 0)) {
                    notificationService.createPaymentNotification(paiement, (int) daysRemaining);
                    notificationsSent++;
                }
            }
        }

        System.out.println("Payment notifications sent: " + notificationsSent);
    }

    /**
     * Daily check for unnotified absences
     * Executed every day at 4:00 PM
     * This method can be developed to check if absences have not been notified
     */
    @Scheduled(cron = "0 0 16 * * ?")
    public void checkUnnotifiedAbsences() {
        System.out.println("Executing check for unnotified absences");

    }

    /**
     * Creation of a weekly notification for all learners
     * Executed every Monday at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 ? * MON")
    public void sendWeeklyNotification() {
        System.out.println("Sending weekly notification");

        String title = "Weekly Summary";
        String message = "Welcome to your weekly summary. Don't forget to check your scheduled sessions for this week.";

        int count = notificationService.createNotificationForAll(title, message, "INFORMATION", false);
        System.out.println("Weekly notifications sent: " + count);
    }
}
