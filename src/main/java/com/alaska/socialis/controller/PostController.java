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

import com.alaska.socialis.exceptions.UserNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.requestModel.NewPostRequest;
import com.alaska.socialis.model.responseModel.SuccessResponse;
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
    public ResponseEntity<SuccessResponse> fetchAllPost(@PathVariable Long userid) throws UserNotFoundException {

        List<Post> allPost = this.postService.fetchAllPost(userid);

        SuccessResponse response = SuccessResponse.builder().data(allPost).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }

    @PostMapping("/post")
    public ResponseEntity<SuccessResponse> createPost(@RequestBody @Valid NewPostRequest post,
            BindingResult validationResult) throws ValidationErrorsException, UserNotFoundException {
        Post newPost = this.postService.createPost(post, validationResult);

        SuccessResponse response = SuccessResponse.builder().data(newPost).status(HttpStatus.CREATED).build();

        // ! dispatch an event to notify followers

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.CREATED);
    }
}
