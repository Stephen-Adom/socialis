package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.dto.AllCommentDto;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.requestModel.CommentRequest;

public interface CommentServiceInterface {

    public CommentDto createComment(CommentRequest comment, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException;

    public List<AllCommentDto> getAllComments(Long userId, Long postId);
}
