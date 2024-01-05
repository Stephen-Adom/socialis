package com.alaska.socialis.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResharedUserDto {
    private Long userId;
    private Boolean withContent;
}
