package org.example.Controller;

import org.example.DTO.NotificationDTO;
import org.example.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true",
             allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST,
             RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH})
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Get all notifications
    @GetMapping
    public ResponseEntity<?> getAllNotifications() {
        try {
            System.out.println("DEBUG: Getting all notifications");
            List<NotificationDTO> notifications = notificationService.getAllNotifications();
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR while getting all notifications: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while getting notifications",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get a notification by its ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable Long id) {
        try {
            System.out.println("DEBUG: Getting notification with ID: " + id);
            NotificationDTO notification = notificationService.getNotificationById(id);
            if (notification != null) {
                return new ResponseEntity<>(notification, HttpStatus.OK);
            }
            return new ResponseEntity<>(
                Map.of("message", "Notification not found with ID: " + id),
                HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("ERROR while getting notification ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while getting the notification",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all notifications for a learner
    @GetMapping("/apprenant/{learnerId}")
    public ResponseEntity<?> getNotificationsByLearner(@PathVariable Long learnerId) {
        try {
            System.out.println("DEBUG: Getting notifications for learner ID: " + learnerId);
            List<NotificationDTO> notifications = notificationService.getNotificationsByLearner(learnerId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR while getting notifications for learner ID " +
                                learnerId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while getting notifications",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get unread notifications for a learner
    @GetMapping("/apprenant/{learnerId}/non-lues")
    public ResponseEntity<?> getUnreadNotificationsByLearner(@PathVariable Long learnerId) {
        try {
            System.out.println("DEBUG: Getting unread notifications for learner ID: " + learnerId);
            List<NotificationDTO> notifications = notificationService.getUnreadNotificationsByLearner(learnerId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR while getting unread notifications for learner ID " +
                                learnerId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while getting unread notifications",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get notifications by type
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getNotificationsByType(@PathVariable String type) {
        try {
            System.out.println("DEBUG: Getting notifications of type: " + type);
            List<NotificationDTO> notifications = notificationService.getNotificationsByType(type);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR while getting notifications of type " +
                                type + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while getting notifications by type",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get urgent notifications
    @GetMapping("/urgentes")
    public ResponseEntity<?> getUrgentNotifications() {
        try {
            System.out.println("DEBUG: Getting urgent notifications");
            List<NotificationDTO> notifications = notificationService.getUrgentNotifications();
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR while getting urgent notifications: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while getting urgent notifications",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Count unread notifications for a learner
    @GetMapping("/apprenant/{learnerId}/count")
    public ResponseEntity<?> countUnreadNotifications(@PathVariable Long learnerId) {
        try {
            System.out.println("DEBUG: Counting unread notifications for learner ID: " + learnerId);
            long count = notificationService.countUnreadNotifications(learnerId);
            return new ResponseEntity<>(Map.of("count", count), HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR while counting notifications for learner ID " +
                                learnerId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while counting unread notifications",
                       "error", e.getMessage(), "count", 0),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Mark a notification as read
    @PatchMapping("/{id}/lire")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            System.out.println("DEBUG: Attempting to mark notification ID " + id + " as read");

            // Validate ID
            if (id == null || id <= 0) {
                System.err.println("ERROR: Invalid notification ID: " + id);
                return new ResponseEntity<>(
                    Map.of("message", "Invalid notification ID"),
                    HttpStatus.BAD_REQUEST);
            }

            // Find and mark notification as read
            NotificationDTO dto = notificationService.markAsRead(id);

            if (dto != null) {
                System.out.println("DEBUG: Notification ID " + id + " marked as read successfully");
                return new ResponseEntity<>(dto, HttpStatus.OK);
            }

            System.err.println("ERROR: Notification not found with ID: " + id);
            return new ResponseEntity<>(
                Map.of("message", "Notification not found with ID: " + id),
                HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("ERROR while marking notification ID " +
                                id + " as read: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while marking the notification as read",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Alternative PUT method to mark a notification as read
    // This method does the exact same thing as the PATCH method above,
    // but uses PUT which is better supported by some frameworks/proxies
    @PutMapping("/{id}/lire")
    public ResponseEntity<?> markAsReadPut(@PathVariable Long id) {
        try {
            System.out.println("DEBUG: Attempting to mark notification ID " + id + " as read (PUT method)");

            // Validate ID
            if (id == null || id <= 0) {
                System.err.println("ERROR: Invalid notification ID: " + id);
                return new ResponseEntity<>(
                    Map.of("message", "Invalid notification ID"),
                    HttpStatus.BAD_REQUEST);
            }

            // Find and mark notification as read
            NotificationDTO dto = notificationService.markAsRead(id);

            if (dto != null) {
                System.out.println("DEBUG: Notification ID " + id + " marked as read successfully (PUT method)");
                return new ResponseEntity<>(dto, HttpStatus.OK);
            }

            System.err.println("ERROR: Notification not found with ID: " + id);
            return new ResponseEntity<>(
                Map.of("message", "Notification not found with ID: " + id),
                HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("ERROR while marking notification ID " +
                                id + " as read (PUT method): " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while marking the notification as read",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Manually create a payment notification
    @PostMapping("/paiement/rappel/{learnerId}")
    public ResponseEntity<?> createPaymentNotification(@PathVariable Long learnerId) {
        try {
            System.out.println("DEBUG: Creating a payment notification for learner ID: " + learnerId);
            NotificationDTO notification = notificationService.createManualPaymentNotification(learnerId);
            if (notification != null) {
                return new ResponseEntity<>(notification, HttpStatus.CREATED);
            }
            return new ResponseEntity<>(
                Map.of("message", "Learner not found with ID: " + learnerId),
                HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("ERROR while creating payment notification for learner ID " +
                                learnerId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while creating the payment notification",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Manually create an urgent test notification
    @PostMapping("/test-urgente/{learnerId}")
    public ResponseEntity<?> createUrgentTestNotification(@PathVariable Long learnerId) {
        try {
            System.out.println("DEBUG: Creating an urgent test notification for learner ID: " + learnerId);
            NotificationDTO notification = notificationService.createUrgentNotification(
                    learnerId,
                    "Urgent test notification",
                    "This is an urgent test notification. Please check its display.",
                    "TEST"
            );

            if (notification != null) {
                return new ResponseEntity<>(notification, HttpStatus.CREATED);
            }
            return new ResponseEntity<>(
                Map.of("message", "Learner not found with ID: " + learnerId),
                HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("ERROR while creating urgent test notification for learner ID " +
                                learnerId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while creating the urgent test notification",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Create multiple test notifications (for debugging)
    @PostMapping("/create-test-notifications/{learnerId}")
    public ResponseEntity<?> createTestNotifications(@PathVariable Long learnerId) {
        try {
            System.out.println("DEBUG: Creating multiple test notifications for learner ID: " + learnerId);

            List<NotificationDTO> createdNotifications = new ArrayList<>();

            // Normal notification
            NotificationDTO normal = notificationService.createUrgentNotification(
                    learnerId,
                    "Normal notification",
                    "This is a normal test notification.",
                    "INFORMATION"
            );
            if (normal != null) createdNotifications.add(normal);

            // Urgent notification
            NotificationDTO urgent = notificationService.createUrgentNotification(
                    learnerId,
                    "URGENT notification",
                    "This is an URGENT test notification.",
                    "URGENT"
            );
            if (urgent != null) createdNotifications.add(urgent);

            // Payment notification
            NotificationDTO payment = notificationService.createManualPaymentNotification(learnerId);
            if (payment != null) createdNotifications.add(payment);

            if (!createdNotifications.isEmpty()) {
                return new ResponseEntity<>(
                    Map.of("message", createdNotifications.size() + " notifications created successfully",
                           "notifications", createdNotifications),
                    HttpStatus.CREATED
                );
            }

            return new ResponseEntity<>(
                Map.of("message", "Learner not found with ID: " + learnerId),
                HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("ERROR while creating test notifications: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(
                Map.of("message", "An error occurred while creating test notifications",
                       "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
