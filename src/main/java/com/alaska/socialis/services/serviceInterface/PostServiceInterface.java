package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.model.dto.PostDto;

public interface PostServiceInterface {
        public void createPost(Long userId, String content, MultipartFile[] multipartFiles)
                        throws EntityNotFoundException;

        public PostDto fetchPostById(String postId) throws EntityNotFoundException;

        public List<PostDto> fetchAllPost();

        public void editPost(Long id, String content, MultipartFile[] multipartFiles)
                        throws EntityNotFoundException;

        public void deletePost(Long id) throws EntityNotFoundException;

        public List<PostDto> fetchAllPostsByUser(Long userId) throws EntityNotFoundException;

        public void repostWithNoContent(Long userId, Long postId)
                        throws EntityNotFoundException, UserAlreadyExistException;
}
