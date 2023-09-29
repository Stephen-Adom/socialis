package com.alaska.socialis.model.dto;

import java.util.Date;
import java.util.Map;

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
    private Date createdAt;
    private Date updatedAt;
    private Map<String, Object> post;
    private Map<String, String> user;

}
