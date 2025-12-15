package com.example.demo.services;

import com.example.demo.models.Notification;
import com.example.demo.models.Utilisateur;
import com.example.demo.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void envoyerNotification(Utilisateur utilisateur, String message, String type) {
        Notification notification = new Notification();
        notification.setUtilisateur(utilisateur);
        notification.setMessage(message);
        notification.setType(type);
        notification.setDate(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
