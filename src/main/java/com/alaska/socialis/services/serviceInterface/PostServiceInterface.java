package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.requestModel.RepostBody;

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

        public void undoRepost(Long postId) throws EntityNotFoundException;

        public void repostWithContent(Long userId, RepostBody requestBody, BindingResult validationResult)
                        throws EntityNotFoundException, ValidationErrorsException, UserAlreadyExistException;
}
