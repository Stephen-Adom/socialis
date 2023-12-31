package com.alaska.socialis.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.StoryDto;
import com.alaska.socialis.model.dto.SuccessMessage;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.model.dto.WatchedStoryDto;
import com.alaska.socialis.services.StoriesService;

@RestController
@RequestMapping("/api/stories")
public class StoriesController {

    @Autowired
    private StoriesService storiesService;

    @GetMapping("/{userId}/all")
    private ResponseEntity<SuccessMessage> fetchAuthUserStories(@PathVariable("userId") Long userId)
            throws EntityNotFoundException {
        this.storiesService.fetchAuthUserStories(userId);

        SuccessMessage message = SuccessMessage.builder().message("User stories fetched")
                .status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessMessage>(message, HttpStatus.OK);
    }

    @PostMapping(value = "/{userId}/upload", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<SuccessMessage> postStories(
            @RequestParam(required = true, value = "storyMedia") MultipartFile file,
            @RequestParam(required = false, value = "caption") String caption, @PathVariable("userId") Long userId)
            throws IOException, EntityNotFoundException {

        this.storiesService.uploadStory(file, caption, userId);

        SuccessMessage message = SuccessMessage.builder().message("User story uploaded successfully")
                .status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessMessage>(message, HttpStatus.OK);
    }

    @GetMapping("/{userId}/watched/{mediaId}")
    public ResponseEntity<SuccessMessage> storyWatchedByUser(
            @PathVariable(required = true, value = "userId") Long userId,
            @PathVariable(required = true, value = "mediaId") Long mediaId) throws EntityNotFoundException {

        this.storiesService.recordStoryWatchedByUser(userId, mediaId);

        SuccessMessage message = SuccessMessage.builder().message("user watched saved")
                .status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessMessage>(message, HttpStatus.OK);
    }

    @GetMapping("/{mediaId}/watched/users")
    public ResponseEntity<SuccessResponse> usersWatchedAMedia(
            @PathVariable(required = true, value = "mediaId") Long mediaId) {

        List<WatchedStoryDto> allwatchedUsers = this.storiesService.usersWatchedAMedia(mediaId);

        SuccessResponse response = SuccessResponse.builder().data(allwatchedUsers).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }

    @GetMapping("/{userId}/following/stories")
    public ResponseEntity<SuccessResponse> getAllStoriesForMyFollowings(
            @PathVariable(required = true, value = "userId") Long userId) throws EntityNotFoundException {
        Set<StoryDto> allFollowingStories = this.storiesService.getAllStoriesForMyFollowings(userId);

        SuccessResponse response = SuccessResponse.builder().data(allFollowingStories).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }
}
