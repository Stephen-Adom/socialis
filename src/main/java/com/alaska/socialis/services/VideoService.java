package com.alaska.socialis.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.JpaSort.Path;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserStory;
import com.alaska.socialis.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class VideoService {
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Value("${app.video-folder}")
    private String videoFolder;

    private final String storyFilePath = "socialis/user/stories";

    public void uploadStory(MultipartFile file, Long userId) throws IOException, EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);
        Map<String, Object> mediaUrl = new HashMap<>();

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (file.getContentType().contains("video")) {
            mediaUrl = this.uploadAndSliceVideo(file);
        } else {
            mediaUrl = this.uploadStoryImage(file);
        }

        if (Objects.nonNull(mediaUrl)) {
            UserStory newStory = new UserStory();
        }
    }

    public Map<String, Object> uploadStoryImage(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult = this.imageUploadService.uploadImageToCloud(storyFilePath, file, "image");

        return uploadResult;
    }

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
        Map<String, String> params = ObjectUtils.asMap(
                "resource_type", "video",
                "folder", storyFilePath);

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
}
