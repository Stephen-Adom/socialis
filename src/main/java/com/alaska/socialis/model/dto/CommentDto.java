package com.alaska.socialis.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alaska.socialis.model.CommentImages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;
    private String content;
    private List<CommentImages> commentImages = new ArrayList<CommentImages>();
    private Date createdAt;
    private Date updatedAt;
    private int numberOfLikes;
    private int numberOfReplies;
    private SimpleUserDto user;

}
