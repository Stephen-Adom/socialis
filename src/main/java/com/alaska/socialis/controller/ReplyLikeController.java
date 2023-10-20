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

import com.alaska.socialis.model.dto.ReplyDto;
import com.alaska.socialis.services.ReplyLikeService;

@RestController
@RequestMapping("/api")
public class ReplyLikeController {
    @Autowired
    private ReplyLikeService replyLikeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{userId}/{replyId}/reply_like")
    public ResponseEntity<Map<String, Object>> toggleReplyLike(@PathVariable("userId") Long userId,
            @PathVariable("replyId") Long replyId) {
        ReplyDto replyDto = this.replyLikeService.toggleReplyLike(userId, replyId);

        this.messagingTemplate.convertAndSend("/feed/reply/update", replyDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK);
        response.put("message", "Reply like updated");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }
}
