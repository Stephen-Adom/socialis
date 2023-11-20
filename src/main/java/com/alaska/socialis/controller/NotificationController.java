package com.alaska.socialis.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alaska.socialis.model.dto.NotificationDto;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.services.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{userId}/all")
    public ResponseEntity<SuccessResponse> fetchAllUserNotifications(@PathVariable("userId") Long userId)
            throws EntityNotFoundException {
        List<NotificationDto> notifications = this.notificationService.fetchAllUserNotification(userId);

        SuccessResponse response = SuccessResponse.builder().data(notifications).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }
}
