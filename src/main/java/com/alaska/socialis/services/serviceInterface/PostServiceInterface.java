package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.UserNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.requestModel.NewPostRequest;

public interface PostServiceInterface {
    public Post createPost(NewPostRequest post, BindingResult validationResult)
            throws ValidationErrorsException, UserNotFoundException;

    public List<Post> fetchAllPost(Long userId) throws UserNotFoundException;
}
