package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.NotificationDto;

public interface NotificationServiceInterface {
    public List<NotificationDto> fetchAllUserNotification(Long userId) throws EntityNotFoundException;
}