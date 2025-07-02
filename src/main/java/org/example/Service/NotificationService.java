package org.example.Service;

import org.example.DTO.NotificationDTO;
import org.example.Model.Apprenant;
import org.example.Model.Notification;
import org.example.Model.Paiement;
import org.example.Model.Presence;
import org.example.Repository.ApprenantRepository;
import org.example.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ApprenantRepository apprenantRepository;

    // Convert Entity to DTO
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitre(notification.getTitre());
        dto.setMessage(notification.getMessage());
        dto.setDateCreation(notification.getDateCreation());
        dto.setDateLecture(notification.getDateLecture());
        dto.setLu(notification.isLu());
        dto.setType(notification.getType());
        dto.setUrgente(notification.isUrgente());

        if (notification.getApprenant() != null) {
            dto.setApprenantId(notification.getApprenant().getId());
            dto.setApprenantNom(notification.getApprenant().getNom());
        }

        return dto;
    }

    // Get all notifications
    public List<NotificationDTO> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get a notification by its ID
    public NotificationDTO getNotificationById(Long id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return notification.map(this::convertToDTO).orElse(null);
    }

    // Get all notifications for a learner
    public List<NotificationDTO> getNotificationsByLearner(Long learnerId) {
        Optional<Apprenant> optApprenant = apprenantRepository.findById(learnerId);
        if (optApprenant.isPresent()) {
            List<Notification> notifications = notificationRepository.findByApprenant(optApprenant.get());
            return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
        }
        return List.of();
    }

    // Get unread notifications for a learner
    public List<NotificationDTO> getUnreadNotificationsByLearner(Long learnerId) {
        Optional<Apprenant> optApprenant = apprenantRepository.findById(learnerId);
        if (optApprenant.isPresent()) {
            List<Notification> notifications = notificationRepository.findByApprenantAndLu(optApprenant.get(), false);
            return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
        }
        return List.of();
    }

    // Get notifications by type
    public List<NotificationDTO> getNotificationsByType(String type) {
        List<Notification> notifications = notificationRepository.findByType(type);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get urgent notifications
    public List<NotificationDTO> getUrgentNotifications() {
        List<Notification> notifications = notificationRepository.findByUrgente(true);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Mark a notification as read
    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isPresent()) {
            Notification notification = optNotification.get();
            notification.setLu(true);
            notification.setDateLecture(LocalDateTime.now());
            return convertToDTO(notificationRepository.save(notification));
        }
        return null;
    }

    // NEW METHODS FOR AUTOMATIC NOTIFICATIONS

    /**
     * Creates an automatic notification for an absence
     * @param presence The presence record marked as absent
     * @return The created notification
     */
    @Transactional
    public Notification createAbsenceNotification(Presence presence) {
        Apprenant apprenant = presence.getApprenant();

        Notification notification = new Notification();
        notification.setTitre("Absence recorded");
        notification.setMessage("You have been marked absent for the session of " +
                presence.getDate().toString() + " for group " + presence.getGroupe().getName());
        notification.setDateCreation(LocalDateTime.now());
        notification.setLu(false);
        notification.setType("ABSENCE");
        notification.setUrgente(true);
        notification.setApprenant(apprenant);

        return notificationRepository.save(notification);
    }

    /**
     * Creates a payment reminder notification
     * @param paiement The concerned payment record
     * @param joursRestants The number of days before the due date
     * @return The created notification
     */
    @Transactional
    public Notification createPaymentNotification(Paiement paiement, int joursRestants) {
        Apprenant apprenant = paiement.getApprenant();
        // All payment notifications are now urgent
        boolean urgente = true;

        Notification notification = new Notification();
        notification.setTitre("Payment Reminder");

        if (joursRestants > 0) {
            notification.setMessage("You have " + joursRestants + " day(s) left to make your payment of "
                    + paiement.getMontant() + " DH before the deadline.");
        } else if (joursRestants == 0) {
            notification.setMessage("Your payment of " + paiement.getMontant()
                    + " DH is due today. Please regularize your situation.");
        } else {
            notification.setMessage("Your payment of " + paiement.getMontant()
                    + " DH is overdue by " + Math.abs(joursRestants) + " day(s). Please regularize your situation quickly.");
        }

        notification.setDateCreation(LocalDateTime.now());
        notification.setLu(false);
        notification.setType("PAIEMENT");
        notification.setUrgente(urgente);
        notification.setApprenant(apprenant);

        return notificationRepository.save(notification);
    }

    /**
     * Generic method to create an urgent notification for a learner
     * @param learnerId ID of the learner
     * @param title Title of the notification
     * @param message Message of the notification
     * @param type Notification type (ABSENCE, PAYMENT, INFORMATION, etc.)
     * @return The created notification or null if the learner is not found
     */
    @Transactional
    public NotificationDTO createUrgentNotification(Long learnerId, String title, String message, String type) {
        Optional<Apprenant> optApprenant = apprenantRepository.findById(learnerId);
        if (!optApprenant.isPresent()) {
            return null;
        }

        Notification notification = new Notification();
        notification.setTitre(title);
        notification.setMessage(message);
        notification.setDateCreation(LocalDateTime.now());
        notification.setLu(false);
        notification.setType(type);
        notification.setUrgente(true);
        notification.setApprenant(optApprenant.get());

        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }

    /**
     * Creates a notification for all learners
     * @param title Title of the notification
     * @param message Message of the notification
     * @param type Notification type (ABSENCE, PAYMENT, INFORMATION, etc.)
     * @param urgent If the notification is urgent
     * @return The number of created notifications
     */
    @Transactional
    public int createNotificationForAll(String title, String message, String type, boolean urgent) {
        List<Apprenant> apprenants = apprenantRepository.findAll();
        int count = 0;

        for (Apprenant apprenant : apprenants) {
            Notification notification = new Notification();
            notification.setTitre(title);
            notification.setMessage(message);
            notification.setDateCreation(LocalDateTime.now());
            notification.setLu(false);
            notification.setType(type);
            notification.setUrgente(urgent);
            notification.setApprenant(apprenant);

            notificationRepository.save(notification);
            count++;
        }

        return count;
    }

    /**
     * Counts the number of unread notifications for a learner
     * @param learnerId ID of the learner
     * @return The number of unread notifications
     */
    public long countUnreadNotifications(Long learnerId) {
        Optional<Apprenant> optApprenant = apprenantRepository.findById(learnerId);
        if (optApprenant.isPresent()) {
            return notificationRepository.countByApprenantAndLu(optApprenant.get(), false);
        }
        return 0;
    }

    /**
     * Creates a manual notification for an overdue payment
     * @param learnerId ID of the concerned learner
     * @return The created notification or null if the learner is not found
     */
    @Transactional
    public NotificationDTO createManualPaymentNotification(Long learnerId) {
        Optional<Apprenant> optApprenant = apprenantRepository.findById(learnerId);
        if (!optApprenant.isPresent()) {
            return null;
        }

        Notification notification = new Notification();
        notification.setTitre("Payment Reminder");
        notification.setMessage("A reminder for your overdue payment. Please regularize your situation as soon as possible.");
        notification.setDateCreation(LocalDateTime.now());
        notification.setLu(false);
        notification.setType("PAIEMENT");
        notification.setUrgente(true);
        notification.setApprenant(optApprenant.get());

        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }
}
