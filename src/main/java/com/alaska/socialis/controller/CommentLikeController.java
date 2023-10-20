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

import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.services.CommentLikeService;

@RestController
@RequestMapping("/api")
public class CommentLikeController {
    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{userId}/{commentId}/comment_like")
    public ResponseEntity<Map<String, Object>> toggleCommentLike(@PathVariable("userId") Long userId,
            @PathVariable("commentId") Long commentId) {
        CommentDto commentDto = this.commentLikeService.toggleCommentLike(userId, commentId);

        this.messagingTemplate.convertAndSend("/feed/comment/update", commentDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK);
        response.put("message", "Comment like updated");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }
}
