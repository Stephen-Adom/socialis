package com.alaska.socialis.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.services.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping(value = "/user/update_cover_background", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> updateUserCoverBackground(
            @RequestParam(required = true, value = "user_id") Long userId,
            @RequestParam(required = true, value = "image") MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        UserDto updatedUser = this.userService.updateUserCoverImage(userId, multipartFile);

        this.messagingTemplate.convertAndSend("/feed/user/update", updatedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK);
        response.put("message", "Cover image background updated!");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/user/update_profile_image", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> updateUserProfileImage(
            @RequestParam(required = true, value = "user_id") Long userId,
            @RequestParam(required = true, value = "image") MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        UserDto updatedUser = this.userService.updateUserProfileImage(userId, multipartFile);

        this.messagingTemplate.convertAndSend("/feed/user/update", updatedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK);
        response.put("message", "Cover image background updated!");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }
}
