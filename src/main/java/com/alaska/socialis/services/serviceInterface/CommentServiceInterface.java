package com.alaska.socialis.services.serviceInterface;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.AllCommentDto;

public interface CommentServiceInterface {

    public Map<String, Object> createComment(Long userId, Long postId, String content, MultipartFile[] multipartFiles)
            throws EntityNotFoundException;

    public List<AllCommentDto> getAllComments(Long userId, Long postId);
}
