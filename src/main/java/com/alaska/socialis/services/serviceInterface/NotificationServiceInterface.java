package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.NotificationDto;

public interface NotificationServiceInterface {
    public List<NotificationDto> fetchAllUserNotification(Long userId) throws EntityNotFoundException;

    public Long getUserUnreadNotificationCount(Long userId) throws EntityNotFoundException;

    public void markNotificationAsRead(Long notificationId) throws EntityNotFoundException;

    public void markAllNotificationAsRead(Long userId) throws EntityNotFoundException;
}
