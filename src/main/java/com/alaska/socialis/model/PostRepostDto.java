package com.alaska.socialis.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PostRepostDto {
    private Long id;
    private String uid;
    private Long user_id;
    private String content;
    private int number_of_likes;
    private int number_of_comments;
    private int number_of_bookmarks;
    private int number_of_repost;
    private LocalDateTime created_at;
    private Long original_post_id;
}
