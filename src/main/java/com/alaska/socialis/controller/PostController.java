package com.alaska.socialis.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.dto.SuccessMessage;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.model.requestModel.NewPostRequest;
import com.alaska.socialis.model.requestModel.UpdatePostRequest;
import com.alaska.socialis.services.PostService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/{userid}/posts")
    public ResponseEntity<SuccessResponse> fetchAllPost(@PathVariable Long userid) throws EntityNotFoundException {

        List<Post> allPost = this.postService.fetchAllPost(userid);

        SuccessResponse response = SuccessResponse.builder().data(allPost).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }

    @PostMapping("/post")
    public ResponseEntity<SuccessResponse> createPost(@RequestBody @Valid NewPostRequest post,
            BindingResult validationResult) throws ValidationErrorsException, EntityNotFoundException {
        Post newPost = this.postService.createPost(post, validationResult);

        SuccessResponse response = SuccessResponse.builder().data(newPost).status(HttpStatus.CREATED).build();

        // ! dispatch an event to notify followers

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/post/{id}/edit")
    public ResponseEntity<SuccessResponse> editPost(@PathVariable Long id, @RequestBody @Valid UpdatePostRequest post,
            BindingResult validationResult) throws ValidationErrorsException, EntityNotFoundException {
        Post updatedPost = this.postService.editPost(id, post, validationResult);

        SuccessResponse response = SuccessResponse.builder().data(updatedPost).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/post/{id}/delete")
    public ResponseEntity<SuccessMessage> deletePost(@PathVariable Long userId, @PathVariable Long id)
            throws EntityNotFoundException {
        this.postService.deletePost(userId, id);

        SuccessMessage response = SuccessMessage.builder().message("Post Successfully deleted").status(HttpStatus.OK)
                .build();

        return new ResponseEntity<SuccessMessage>(response, HttpStatus.OK);
    }
}
