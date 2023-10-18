package com.alaska.socialis.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.services.PostLikeService;

@RestController
@RequestMapping("/api")
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

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
}
