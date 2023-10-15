package com.alaska.socialis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleUserDto {

    private Long id;

    private String firstname;

    private String lastname;

    private String username;

    private String imageUrl;

    private String bio;
}
