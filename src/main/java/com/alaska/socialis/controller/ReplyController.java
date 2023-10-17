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
import com.alaska.socialis.model.dto.ReplyDto;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.services.ReplyService;

@RestController
@RequestMapping("/api")
public class ReplyController {

    @Autowired
    private ReplyService replyService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping(value = "/reply", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> createCommentReply(
            @RequestParam(required = true, value = "user_id") Long userId,
            @RequestParam(required = true, value = "comment_id") Long commentId,
            @RequestParam(required = false, value = "content") String content,
            @RequestParam(required = false, value = "images") MultipartFile[] multipartFiles)
            throws EntityNotFoundException {

        Map<String, Object> replyResponse = this.replyService.createReply(userId, commentId, content, multipartFiles);

        this.messagingTemplate.convertAndSend("/feed/comment/update", replyResponse.get("commentDto"));
        this.messagingTemplate.convertAndSend("/feed/reply/new", replyResponse.get("replyDto"));

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.CREATED);
        response.put("message", "New Reply Created");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);

    }

    @GetMapping("/{commentId}/all_replies")
    public ResponseEntity<SuccessResponse> allReplies(@PathVariable("commentId") Long commentId) {
        List<ReplyDto> allReplyDto = this.replyService.fetchAllReplies(commentId);

        SuccessResponse response = SuccessResponse.builder().data(allReplyDto).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }
}
