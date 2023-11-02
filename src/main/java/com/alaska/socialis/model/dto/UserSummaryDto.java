package com.alaska.socialis.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSummaryDto {
    private String firstname;
    private String lastname;
    private String username;
    private String bio;
    private String imageUrl;
    private int totalPost = 0;
    private int followers = 0;
    private int following = 0;
}
