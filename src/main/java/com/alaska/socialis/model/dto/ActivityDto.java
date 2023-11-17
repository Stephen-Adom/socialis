package com.alaska.socialis.model.dto;

import java.util.Date;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityDto {
    private Long id;

    private Object actionType;

    private Object groupType;

    private Date createdAt;

    private Object activityLink;

    private Object targetData;
}
