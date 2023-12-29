package com.alaska.socialis.model.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WatchedStoryDto {

    private Long id;

    private SimpleUserDto user;

    private Date watchedAt;
}
