package com.alaska.socialis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alaska.socialis.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
