package com.alaska.socialis.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alaska.socialis.model.PostImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    private Long id;
    private String uid;
    private String content;
    private int numberOfLikes = 0;
    private int numberOfComments = 0;
    private int numberOfBookmarks = 0;
    private Date createdAt;
    private Date updatedAt;
    private SimpleUserDto user;
    List<PostImage> postImages = new ArrayList<PostImage>();
    List<LikeDto> likes = new ArrayList<LikeDto>();
    List<Long> bookmarkedUsers = new ArrayList<Long>();
}
