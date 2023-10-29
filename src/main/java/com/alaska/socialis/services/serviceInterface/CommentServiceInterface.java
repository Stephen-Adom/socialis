package com.alaska.socialis.services.serviceInterface;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.dto.CommentDto;

public interface CommentServiceInterface {

        public Map<String, Object> createComment(Long userId, Long postId, String content,
                        MultipartFile[] multipartFiles)
                        throws EntityNotFoundException;

        public List<CommentDto> getAllComments(Long postId);

        public Comment editComment(Long id, String content, MultipartFile[] multipartFiles)
                        throws EntityNotFoundException;

        public void deleteComment(Long id) throws EntityNotFoundException;

        public CommentDto fetchCommentById(Long postId) throws EntityNotFoundException;
}
