package com.alaska.socialis.model.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSummaryFollowingDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String username;
    private String bio;
    private String imageUrl;
    private String coverImageUrl;
    private String phonenumber;
    private String address;
    private int totalPost = 0;
    private int followers = 0;
    private int following = 0;
    Set<String> followersList = new HashSet<>();
    Set<String> followingList = new HashSet<>();
}
