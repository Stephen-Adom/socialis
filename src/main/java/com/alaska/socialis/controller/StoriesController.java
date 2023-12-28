package com.alaska.socialis.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import com.alaska.socialis.model.Story;
import com.alaska.socialis.model.StoryMedia;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.StoryDto;
import com.alaska.socialis.model.dto.SuccessMessage;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.repository.StoryMediaRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.StoriesService;

@RestController
@RequestMapping("/api/stories")
public class StoriesController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryMediaRepository storyMediaRepository;

    @GetMapping("/{userId}/all")
    private void fetchAuthUserStories(@PathVariable("userId") Long userId)
            throws EntityNotFoundException {
        // List<StoryDto> userStories =
        // this.storiesservice.fetchAuthUserStories(userId);

        // SuccessResponse response =
        // SuccessResponse.builder().data(userStories).status(HttpStatus.OK).build();

        // return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }

    // @PostMapping(value = "/{userId}/upload", headers =
    // "Content-Type=multipart/form-data")
    // public ResponseEntity<SuccessMessage> postStories(
    // @RequestParam(required = true, value = "storyMedia") MultipartFile file,
    // @RequestParam(required = false, value = "caption") String caption,
    // @PathVariable("userId") Long userId)
    // throws IOException, EntityNotFoundException {

    // this.storiesservice.uploadStory(file, caption, userId);

    // SuccessMessage message = SuccessMessage.builder().message("User story
    // uploaded successfully")
    // .status(HttpStatus.OK).build();

    // return new ResponseEntity<SuccessMessage>(message, HttpStatus.OK);
    // }

    @GetMapping(value = "/upload/story/test")
    public void uploadStoryTest() {
        Optional<User> user = this.userRepository.findById((long) 1);
        Story story = new Story();
        story.setUser(user.get());
        story.setLastUpdated(LocalDateTime.now());

        StoryMedia newMedia = new StoryMedia();
        newMedia.setStory(story);
        newMedia.setMediaUrl("new url");
        newMedia.setMediaCaption("caption");
        newMedia.setMediaType("image");
        newMedia.setExpiredAt(new Date());
        newMedia.setUploadedAt(new Date());

        storyMediaRepository.save(newMedia);

    }
}
