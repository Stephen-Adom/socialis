package com.alaska.socialis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.alaska.socialis.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = { "user", "source" })
    public List<Notification> findFirst40ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(value = "SELECT COUNT(*) from notification u WHERE u.user_id=?1 AND u.is_read=false", nativeQuery = true)
    public Long countAllByUserIdUnreadTrue(Long userId);

    @Query(value = "SELECT * FROM notification u WHERE u.user_id=?1 AND u.is_read=false", nativeQuery = true)
    public List<Notification> allUnreadNotifications(Long userId);
}
