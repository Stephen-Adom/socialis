package com.alaska.socialis.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.model.requestModel.CommentRequest;
import com.alaska.socialis.services.CommentService;
import com.alaska.socialis.model.dto.AllCommentDto;

@RestController
@RequestMapping("/api")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/{userId}/{postId}/comments")
    public ResponseEntity<SuccessResponse> getAllComments(@PathVariable Long userId, @PathVariable Long postId) {
        List<AllCommentDto> comments = this.commentService.getAllComments(userId, postId);

        SuccessResponse response = SuccessResponse.builder().status(HttpStatus.OK).data(comments).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }

    @PostMapping("/comment")
    public ResponseEntity<SuccessResponse> createComment(@RequestBody CommentRequest comment,
            BindingResult validationResult) throws ValidationErrorsException, EntityNotFoundException {
        CommentDto commentDto = this.commentService.createComment(comment, validationResult);

        SuccessResponse response = SuccessResponse.builder().status(HttpStatus.CREATED).data(commentDto).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.CREATED);
    }
}
