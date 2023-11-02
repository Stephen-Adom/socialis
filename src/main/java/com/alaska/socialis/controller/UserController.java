package com.alaska.socialis.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.model.dto.UserSummaryDto;
import com.alaska.socialis.model.requestModel.UserInfoRequeset;
import com.alaska.socialis.services.UserService;

import jakarta.validation.Valid;

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
        response.put("message", "Profile image updated!");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @PostMapping("/user/{id}/update_user_info")
    public ResponseEntity<Map<String, Object>> updateUserInfo(@PathVariable("id") Long userId,
            @RequestBody @Valid UserInfoRequeset requestBody, BindingResult bindingResult)
            throws EntityNotFoundException, ValidationErrorsException {
        UserDto updatedUser = this.userService.updateUserInfo(userId, requestBody, bindingResult);

        this.messagingTemplate.convertAndSend("/feed/user/update", updatedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK);
        response.put("message", "Profile image updated!");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @GetMapping("/user/{username}/info")
    public ResponseEntity<SuccessResponse> fetchUserInfoSummary(@PathVariable("username") String username)
            throws EntityNotFoundException {
        UserSummaryDto userInfo = this.userService.fetchUserInformationByUsername(username);

        SuccessResponse response = SuccessResponse.builder().data(userInfo).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }
}
