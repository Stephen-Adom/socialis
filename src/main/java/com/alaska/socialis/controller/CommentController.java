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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.services.CommentService;
import com.alaska.socialis.model.dto.AllCommentDto;

@RestController
@RequestMapping("/api")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{userId}/{postId}/comments")
    public ResponseEntity<SuccessResponse> getAllComments(@PathVariable Long userId, @PathVariable Long postId) {
        List<AllCommentDto> comments = this.commentService.getAllComments(userId, postId);

        SuccessResponse response = SuccessResponse.builder().status(HttpStatus.OK).data(comments).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/comment", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestParam(required = true, value = "user_id") Long userId,
            @RequestParam(required = true, value = "post_id") Long postId, @RequestParam("content") String content,
            @RequestParam("images") MultipartFile[] multipartFile)
            throws EntityNotFoundException {
        Map<String, Object> commentResponse = this.commentService.createComment(userId, postId, content,
                multipartFile);

        this.messagingTemplate.convertAndSend("/feed/comment/new", commentResponse.get("commentDto"));
        this.messagingTemplate.convertAndSend("/feed/post/update", commentResponse.get("commentDto"));

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.CREATED);
        response.put("message", "New Comment Created");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }
}
