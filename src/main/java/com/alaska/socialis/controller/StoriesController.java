package com.alaska.socialis.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.SuccessMessage;
import com.alaska.socialis.services.VideoService;

@RestController
@RequestMapping("/api/stories")
public class StoriesController {

    @Autowired
    private VideoService videoservice;

    @PostMapping(value = "/{userId}/upload", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<SuccessMessage> postStories(
            @RequestParam(required = true, value = "storyMedia") MultipartFile file,
            @RequestParam(required = false, value = "caption") String caption,
            @PathVariable("userId") Long userId)
            throws IOException, EntityNotFoundException {

        this.videoservice.uploadStory(file, caption, userId);

        SuccessMessage message = SuccessMessage.builder().message("User story uploaded successfully")
                .status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessMessage>(message, HttpStatus.OK);
    }
}
