package com.alaska.socialis.model.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoryDto {

    private Long id;

    private SimpleUserDto user;

    private int numberOfMedia;

    private List<StoryMediaDto> storyMedia = new ArrayList<>();
}
