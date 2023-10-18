package com.alaska.socialis.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.model.dto.SuccessMessage;
import com.alaska.socialis.model.dto.SuccessResponse;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/all_posts")
    public ResponseEntity<Map<String, Object>> fetchAllPost() {

        List<PostDto> postDto = this.postService.fetchAllPost();

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", postDto);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/post", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> createPost(@RequestParam(required = true, value = "user_id") Long userId,
            @RequestParam(required = false, value = "content") String postContent,
            @RequestParam(required = false, value = "images") MultipartFile[] multipartFile)
            throws EntityNotFoundException {

        Post newPost = this.postService.createPost(userId, postContent,
                multipartFile);

        PostDto formattedPost = this.postService.buildPostDto(newPost);

        messagingTemplate.convertAndSend("/feed/post/new", formattedPost);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.CREATED);
        response.put("message", "New Post Created");

        // ! dispatch an event to notify followers

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/post")
    public ResponseEntity<Map<String, Object>> fetchPostDetail(@PathVariable("id") Long postId)
            throws EntityNotFoundException {

        PostDto postDto = this.postService.fetchPostById(postId);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", postDto);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
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
