package com.alaska.socialis.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alaska.socialis.model.PostImage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostDto {
    private Long id;

    private String uid;

    private String content;

    @Builder.Default
    private int numberOfLikes = 0;

    @Builder.Default
    private int numberOfComments = 0;

    @Builder.Default
    private int numberOfBookmarks = 0;

    @Builder.Default
    private int numberOfRepost = 0;

    private Date createdAt;

    private Date updatedAt;

    private SimpleUserDto user;

    @Builder.Default
    List<PostImage> postImages = new ArrayList<PostImage>();

    @Builder.Default
    List<LikeDto> likes = new ArrayList<LikeDto>();

    @Builder.Default
    List<Long> bookmarkedUsers = new ArrayList<Long>();

    private SinglePostDto originalPost;

    @Builder.Default
    List<ResharedUserDto> resharedBy = new ArrayList<>();
}
