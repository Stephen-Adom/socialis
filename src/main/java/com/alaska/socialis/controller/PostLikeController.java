package com.alaska.socialis.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.services.PostLikeService;
import com.alaska.socialis.services.PostService;

@RestController
@RequestMapping("/api")
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostService postService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{userId}/{postId}/like")
    public ResponseEntity<Map<String, Object>> togglePostLike(@PathVariable("userId") Long userId,
            @PathVariable("postId") Long postId) {
        PostDto postDto = this.postLikeService.togglePostLike(userId, postId);

        this.messagingTemplate.convertAndSend("/feed/post/update", postDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK);
        response.put("message", "Post like updated");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/likes")
    public ResponseEntity<Map<String, Object>> fetchAllLikesByUser(@PathVariable("userId") Long userId)
            throws EntityNotFoundException {
        List<Object> allLikes = this.postLikeService.fetchAllLikesByUser(userId);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", allLikes);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }
}
