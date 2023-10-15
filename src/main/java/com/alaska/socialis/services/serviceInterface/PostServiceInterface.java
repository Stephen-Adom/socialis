package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.requestModel.UpdatePostRequest;

public interface PostServiceInterface {
        public Post createPost(Long userId, String content, MultipartFile[] multipartFiles)
                        throws EntityNotFoundException;

        public List<Post> fetchAllPost();

        public Post editPost(Long id, UpdatePostRequest post, BindingResult validationResult)
                        throws ValidationErrorsException, EntityNotFoundException;

        public void deletePost(Long userId, Long id) throws EntityNotFoundException;
}
