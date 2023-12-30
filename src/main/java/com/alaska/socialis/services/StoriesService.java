package com.alaska.socialis.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Story;
import com.alaska.socialis.model.StoryMedia;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.WatchedStory;
import com.alaska.socialis.model.dto.StoryDto;
import com.alaska.socialis.model.dto.WatchedStoryDto;
import com.alaska.socialis.repository.StoryMediaRepository;
import com.alaska.socialis.repository.StoryRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.repository.WatchedStoryRepository;
import com.alaska.socialis.services.serviceInterface.StoriesServiceInterface;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class StoriesService implements StoriesServiceInterface {
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private StoryMediaRepository storyMediaRepository;

    @Autowired
    private WatchedStoryRepository watchedStoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${app.video-folder}")
    private String videoFolder;

    private final String storyFilePath = "socialis/user/stories";

    @Override
    public StoryDto fetchAuthUserStories(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Story allAuthStories = this.storyRepository.findByUserIdOrderByLastUpdatedDesc(userId);

        StoryDto allStories = this.buildUserStory(allAuthStories);

        return allStories;
    }

    @Override
    public void uploadStory(MultipartFile file, String caption, Long userId)
            throws IOException, EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);
        Map<String, Object> mediaObject = new HashMap<>();

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (file.getContentType().contains("video")) {
            mediaObject = this.uploadAndSliceVideo(file);
        } else {
            mediaObject = this.uploadStoryImage(file);
        }

        if (Objects.nonNull(mediaObject)) {
            String mediaUrl = (String) mediaObject.get("secure_url");
            String mediaType = (String) mediaObject.get("resource_type");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            Date currentDate = calendar.getTime();

            calendar.add(Calendar.HOUR, 24);

            Date expiredDate = calendar.getTime();

            Optional<Story> storyExist = this.storyRepository.findByUserId(userId);

            if (storyExist.isEmpty()) {
                // create new story instance
                Story newStory = new Story();
                newStory.setUser(user.get());
                newStory.setNumberOfMedia(newStory.getNumberOfMedia() + 1);
                newStory.setLastUpdated(LocalDateTime.now());

                this.storyRepository.save(newStory);

                // create story media object
                this.createStoryMedia(newStory, mediaUrl, caption, mediaType, currentDate, expiredDate);
            } else {

                Story story = storyExist.get();
                story.setNumberOfMedia(story.getNumberOfMedia() + 1);
                story.setLastUpdated(LocalDateTime.now());

                this.createStoryMedia(story, mediaUrl, caption, mediaType, currentDate, expiredDate);
            }

        }
    }

    private StoryMedia createStoryMedia(Story story, String mediaUrl, String caption, String mediaType,
            Date currentDate, Date expiredDate) {
        StoryMedia newMedia = new StoryMedia();
        newMedia.setStory(story);
        newMedia.setMediaUrl(mediaUrl);
        newMedia.setMediaCaption(caption);
        newMedia.setMediaType(mediaType);
        newMedia.setUploadedAt(currentDate);
        newMedia.setExpiredAt(expiredDate);

        return this.storyMediaRepository.save(newMedia);
    }

    @Override
    public Map<String, Object> uploadStoryImage(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult = this.imageUploadService.uploadImageToCloud(storyFilePath, file, "image");

        return uploadResult;
    }

    @Override
    public Map<String, Object> uploadAndSliceVideo(MultipartFile file) throws IOException {

        // Save the uploaded file to a temporary location
        File tempFile = File.createTempFile("temp", null);
        file.transferTo(tempFile);

        // Step 1: Use FFmpeg to slice the video
        String slicedVideoPath = videoFolder + "/output.mp4";

        String ffmpegCommand = "ffmpeg -i " + tempFile.getAbsolutePath() + " -ss 0 -t 60 -c copy " + slicedVideoPath;

        Process process = Runtime.getRuntime().exec(ffmpegCommand);

        // Wait for the process to finish
        int exitValue = 0;
        try {
            exitValue = process.waitFor();
            // Get the error stream
            InputStream errorStream = process.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
            String line;

            // Log error messages
            while ((line = reader.readLine()) != null) {
                System.err.println(line);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if FFmpeg completed successfully
        if (exitValue != 0) {
            throw new IOException("FFmpeg process did not complete successfully");
        }

        // Step 2: Upload the sliced video to Cloudinary
        Map<String, String> params = ObjectUtils.asMap("resource_type", "video", "folder", storyFilePath);

        Map<String, Object> uploadResult = cloudinary.uploader().upload(slicedVideoPath, params);

        // Clean up: Delete the temporary sliced video file
        File slicedVideoFile = new File(slicedVideoPath);
        if (slicedVideoFile.exists()) {
            slicedVideoFile.delete();
        }

        // Clean up the temporary file
        tempFile.delete();

        // Return the public URL of the uploaded video
        return uploadResult;
    }

    @Override
    public WatchedStoryDto recordStoryWatchedByUser(Long userId, Long mediaId)
            throws EntityNotFoundException {
        Optional<User> userExist = this.userRepository.findById(userId);
        Optional<StoryMedia> storyMediaExist = this.storyMediaRepository.findById(mediaId);

        if (userExist.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " not found",
                    HttpStatus.NOT_FOUND);
        }

        if (storyMediaExist.isEmpty()) {
            throw new EntityNotFoundException("Story media with id " + mediaId + " not found", HttpStatus.NOT_FOUND);
        }

        Optional<WatchedStory> watchedUserExist = this.watchedStoryRepository.findByUserIdAndMediaId(userId, mediaId);

        if (watchedUserExist.isPresent()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            watchedUserExist.get().setWatchedAt(calendar.getTime());

            WatchedStory savedWatched = this.watchedStoryRepository.save(watchedUserExist.get());

            return this.buildWatchStory(savedWatched);

        } else {
            WatchedStory newWatched = new WatchedStory();
            newWatched.setUser(userExist.get());
            newWatched.setMedia(storyMediaExist.get());

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            newWatched.setWatchedAt(calendar.getTime());

            WatchedStory savedWatched = this.watchedStoryRepository.save(newWatched);

            return this.buildWatchStory(savedWatched);
        }

    }

    @Override
    public List<WatchedStoryDto> usersWatchedAMedia(Long mediaId) {
        List<WatchedStory> allWatchedStories = this.watchedStoryRepository
                .findAllByMediaIdOrderByWatchedAtDesc(mediaId);

        List<WatchedStoryDto> allWatchedStoryDto = allWatchedStories.stream()
                .map(watched -> this.buildWatchStory(watched)).collect(Collectors.toList());

        return allWatchedStoryDto;
    }

    @Override
    public StoryDto buildUserStory(Story userStories) {

        StoryDto allStories = this.modelMapper.map(userStories, StoryDto.class);

        return allStories;
    }

    private WatchedStoryDto buildWatchStory(WatchedStory savedWatched) {
        return this.modelMapper.map(savedWatched, WatchedStoryDto.class);
    }
}
