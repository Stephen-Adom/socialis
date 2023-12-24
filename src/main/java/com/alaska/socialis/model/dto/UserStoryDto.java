package com.alaska.socialis.model.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStoryDto {
    private SimpleUserDto user;
    private List<StoryDto> stories = new ArrayList<>();
}
