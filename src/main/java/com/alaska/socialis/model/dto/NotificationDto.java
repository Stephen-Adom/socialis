package com.alaska.socialis.model.dto;

import java.util.Date;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDto {
    private Long id;

    private UserSummary2Dto user;

    private String activityType;

    private UserSummary2Dto source;

    private Map<String, Object> target;

    private String targetType;

    private boolean isRead = false;

    private Date readAt;

    private Date createdAt;
}
