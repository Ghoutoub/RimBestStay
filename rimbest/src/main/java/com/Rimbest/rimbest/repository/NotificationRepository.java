package com.Rimbest.rimbest.repository;

import com.Rimbest.rimbest.model.Notification;
import com.Rimbest.rimbest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndReadFalse(User user);
}
