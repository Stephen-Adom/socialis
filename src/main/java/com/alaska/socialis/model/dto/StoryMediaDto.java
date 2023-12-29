package com.alaska.socialis.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private List<WatchedStoryDto> watchedBy = new ArrayList<>();
}