package com.alaska.socialis.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SuccessMessage;
import com.alaska.socialis.model.requestModel.RepostBody;
import com.alaska.socialis.services.PostService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/all_posts_offset")
    public ResponseEntity<Map<String, Object>> fetchAllPostUsingOffset(@RequestParam(required = true) int offset) {

        List<PostDto> postDto = this.postService.fetchAllPostUsingOffsetFilteringAndWindowIterator(offset);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", postDto);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @GetMapping("/all_posts")
    public ResponseEntity<Map<String, Object>> fetchAllPost() {

        List<PostDto> postDto = this.postService.fetchAllPost();

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", postDto);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<Map<String, Object>> fetchAllPostsByUser(@PathVariable("userId") Long userId)
            throws EntityNotFoundException {
        List<PostDto> allPosts = this.postService.fetchAllPostsByUser(userId);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", allPosts);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/post", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> createPost(@RequestParam(required = true, value = "user_id") Long userId,
            @RequestParam(required = false, value = "content") String postContent,
            @RequestParam(required = false, value = "images") MultipartFile[] multipartFile)
            throws EntityNotFoundException {

        this.postService.createPost(userId, postContent, multipartFile);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.CREATED);
        response.put("message", "New Post Created");

        // ! dispatch an event to notify followers

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/post")
    public ResponseEntity<Map<String, Object>> fetchPostDetail(@PathVariable("id") String postId)
            throws EntityNotFoundException {

        PostDto postDto = this.postService.fetchPostById(postId);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", postDto);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @PatchMapping(value = "/post/{id}/edit", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> editPost(@PathVariable Long id,
            @RequestParam(required = false, value = "content") String postContent,
            @RequestParam(required = false, value = "images") MultipartFile[] multipartFile)
            throws ValidationErrorsException, EntityNotFoundException {
        this.postService.editPost(id, postContent, multipartFile);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("message", "Post Updated");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @DeleteMapping("/post/{id}/delete")
    public ResponseEntity<SuccessMessage> deletePost(@PathVariable Long id)
            throws EntityNotFoundException {
        this.postService.deletePost(id);

        SuccessMessage response = SuccessMessage.builder().message("Post Successfully deleted").status(HttpStatus.OK)
                .build();

        return new ResponseEntity<SuccessMessage>(response, HttpStatus.OK);
    }

    @GetMapping("/post/{userId}/repost/{postId}")
    public ResponseEntity<SuccessMessage> repostWithNoContent(
            @PathVariable(required = true, value = "userId") Long userId,
            @PathVariable(required = true, value = "postId") Long postId)
            throws EntityNotFoundException, UserAlreadyExistException {

        this.postService.repostWithNoContent(userId, postId);

        SuccessMessage response = SuccessMessage.builder().message("Repost successful").status(HttpStatus.OK)
                .build();

        return new ResponseEntity<SuccessMessage>(response, HttpStatus.OK);
    }

    @PostMapping("/post/{userId}/repost")
    public ResponseEntity<SuccessMessage> repostWithContent(
            @PathVariable(required = true, value = "userId") Long userId, @RequestBody @Valid RepostBody requestBody,
            BindingResult validationResult)
            throws EntityNotFoundException, UserAlreadyExistException, ValidationErrorsException {

        this.postService.repostWithContent(userId, requestBody, validationResult);

        SuccessMessage response = SuccessMessage.builder().message("Repost successful").status(HttpStatus.OK)
                .build();

        return new ResponseEntity<SuccessMessage>(response, HttpStatus.OK);
    }

    @GetMapping("/post/remove/repost/{postId}")
    public ResponseEntity<SuccessMessage> undoRepost(@PathVariable(required = true, value = "postId") Long postId)
            throws EntityNotFoundException {

        this.postService.undoRepost(postId);

        SuccessMessage response = SuccessMessage.builder().message("Repost deleted successfully").status(HttpStatus.OK)
                .build();

        return new ResponseEntity<SuccessMessage>(response, HttpStatus.OK);
    }
}
