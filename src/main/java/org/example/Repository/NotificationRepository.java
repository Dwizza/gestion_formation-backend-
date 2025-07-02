package org.example.Repository;

import org.example.Model.Notification;
import org.example.Model.Apprenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByApprenant(Apprenant apprenant);

    List<Notification> findByApprenantAndLu(Apprenant apprenant, boolean lu);

    List<Notification> findByType(String type);

    List<Notification> findByUrgente(boolean urgente);

    long countByApprenantAndLu(Apprenant apprenant, boolean lu);
}
