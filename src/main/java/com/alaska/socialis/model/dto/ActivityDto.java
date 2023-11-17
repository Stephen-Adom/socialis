package com.alaska.socialis.model.dto;

import java.util.Date;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityDto {
    private Long id;

    private String actionType;

    private String groupType;

    private Date createdAt;

    private String activityLink;

    private Map<String, Object> targetData;
}
