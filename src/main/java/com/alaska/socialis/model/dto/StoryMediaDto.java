package com.alaska.socialis.model.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoryMediaDto {
    private Long id;

    private String mediaUrl;

    private String mediaCaption;

    private String mediaType;

    private Date expiredAt;

    private Date uploadedAt;

    private Set<WatchedStoryDto> watchedBy = new HashSet<>();
}