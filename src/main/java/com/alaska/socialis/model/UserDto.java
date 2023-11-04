package com.alaska.socialis.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String uid;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private boolean enabled;
    private Date createdAt;
    private Date updatedAt;
    private int loginCount;
    private String imageUrl;
    private String bio;
    private String coverImageUrl;
    private String phonenumber;
    private String address;
    private int noOfFollowers;
    private int noOfFollowing;
}
