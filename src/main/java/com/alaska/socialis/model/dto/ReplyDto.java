package com.alaska.socialis.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alaska.socialis.model.ReplyImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyDto {

    private Long id;
    private String content;
    private List<ReplyImage> replyImages = new ArrayList<ReplyImage>();
    private Date createdAt;
    private Date updatedAt;
    private int numberOfLikes;
    private SimpleUserDto user;
    List<LikeDto> likes = new ArrayList<LikeDto>();
}
