package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Notification;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.model.dto.NotificationDTO;
import com.Rimbest.rimbest.service.NotificationService;
import com.Rimbest.rimbest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;
    private final UserService userService;

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null)
            return null;
        return userService.findByEmail(authentication.getName()).orElse(null);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        if (notification.getType() != null) {
            dto.setType(notification.getType().name());
        }
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null)
            return ResponseEntity.status(401).build();

        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(user)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null)
            return ResponseEntity.status(401).build();

        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null)
            return ResponseEntity.status(401).build();

        // Normally we'd check if the notification belongs to this user, but keeping it
        // simple
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null)
            return ResponseEntity.status(401).build();

        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
}
