package com.alaska.socialis.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.ffmpeg.FFmpegUtilsService;
import com.alaska.socialis.ffmpeg.TranscodeConfig;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SuccessMessage;
import com.alaska.socialis.services.PostService;
import com.alaska.socialis.services.VideoService;
import com.cloudinary.Cloudinary;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@Slf4j
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private FFmpegUtilsService ffmpegservice;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private VideoService videoservice;

    private static final Logger LOGGER = LoggerFactory.getLogger(PostController.class);

    @Value("${app.video-folder}")
    private String videoFolder;

    private Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));

    @GetMapping("/all_posts_offset")
    public ResponseEntity<Map<String, Object>> fetchAllPostUsingOffset(@RequestParam(required = true) int offset) {

        List<PostDto> postDto = this.postService.fetchAllPostUsingOffsetFilteringAndWindowIterator(offset);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", postDto);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @GetMapping("/all_posts")
    public ResponseEntity<Map<String, Object>> fetchAllPost() {

        List<PostDto> postDto = this.postService.fetchAllPost();

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", postDto);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<Map<String, Object>> fetchAllPostsByUser(@PathVariable("userId") Long userId)
            throws EntityNotFoundException {
        List<PostDto> allPosts = this.postService.fetchAllPostsByUser(userId);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", allPosts);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/post", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> createPost(@RequestParam(required = true, value = "user_id") Long userId,
            @RequestParam(required = false, value = "content") String postContent,
            @RequestParam(required = false, value = "images") MultipartFile[] multipartFile)
            throws EntityNotFoundException {

        this.postService.createPost(userId, postContent, multipartFile);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.CREATED);
        response.put("message", "New Post Created");

        // ! dispatch an event to notify followers

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/post")
    public ResponseEntity<Map<String, Object>> fetchPostDetail(@PathVariable("id") String postId)
            throws EntityNotFoundException {

        PostDto postDto = this.postService.fetchPostById(postId);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", postDto);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @PatchMapping(value = "/post/{id}/edit", headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Map<String, Object>> editPost(@PathVariable Long id,
            @RequestParam(required = false, value = "content") String postContent,
            @RequestParam(required = false, value = "images") MultipartFile[] multipartFile)
            throws ValidationErrorsException, EntityNotFoundException {
        this.postService.editPost(id, postContent, multipartFile);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("message", "Post Updated");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @DeleteMapping("/post/{id}/delete")
    public ResponseEntity<SuccessMessage> deletePost(@PathVariable Long id)
            throws EntityNotFoundException {
        this.postService.deletePost(id);

        SuccessMessage response = SuccessMessage.builder().message("Post Successfully deleted").status(HttpStatus.OK)
                .build();

        return new ResponseEntity<SuccessMessage>(response, HttpStatus.OK);
    }

    // @PostMapping(value = "/stories", headers =
    // "Content-Type=multipart/form-data")
    // public Object postStories(@RequestParam(required = true, value = "video")
    // MultipartFile video)
    // throws IOException {

    // System.out.println(
    // "===================================== uploaded video
    // =======================================");
    // System.out.println(video);

    // TranscodeConfig transcodeConfig = new TranscodeConfig();
    // transcodeConfig.setCutEnd("");
    // transcodeConfig.setCutStart("");
    // transcodeConfig.setPoster("00:00:00.001");
    // transcodeConfig.setTsSeconds("15");

    // LOGGER.info("File Information：title={}, size={}",
    // video.getOriginalFilename(),
    // video.getSize());
    // LOGGER.info("Transcoding configuration：{}", transcodeConfig);

    // // The name of the original file, which is the title of the video
    // String title = video.getOriginalFilename();

    // // io to temporary files
    // Path tempFile = tempDir.resolve(title);
    // LOGGER.info("io to temporary files：{}", tempFile.toString());

    // try {

    // video.transferTo(tempFile);

    // // Remove the suffix
    // title = title.substring(0, title.lastIndexOf("."));

    // // Generate subdirectories by date
    // String today =
    // DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());

    // // Try creating a video catalog
    // Path targetFolder = Files.createDirectories(Paths.get(videoFolder, today,
    // title));

    // LOGGER.info("target folder：{}", targetFolder);
    // Files.createDirectories(targetFolder);

    // // Start transcoding
    // LOGGER.info("Start transcoding");
    // try {
    // // FFmpegUtils fFmpegUtils = new FFmpegUtils();
    // ffmpegservice.sliceVideo(tempFile.toString(), targetFolder.toString(),
    // transcodeConfig);
    // } catch (Exception e) {
    // LOGGER.error("The transcoding is abnormal：{}", e.getMessage());
    // Map<String, Object> result = new HashMap<>();
    // result.put("success", false);
    // result.put("message", e.getMessage());

    // System.out.println(result);
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    // }

    // // Encapsulation results
    // Map<String, Object> videoInfo = new HashMap<>();
    // videoInfo.put("title", title);
    // videoInfo.put("m3u8", videoFolder + String.join("/", "", today, title,
    // "index.m3u8"));
    // videoInfo.put("poster", videoFolder + String.join("/", "", today, title,
    // "poster.jpg"));

    // Map<String, Object> result = new HashMap<>();
    // result.put("success", true);
    // result.put("data", videoInfo);
    // System.out.println(result);
    // return result;
    // } finally {
    // // Always delete temporary files
    // Files.delete(tempFile);
    // }
    // }

    @PostMapping(value = "/stories", headers = "Content-Type=multipart/form-data")
    public String postStories(@RequestParam(required = true, value = "video") MultipartFile file)
            throws IOException {

        // Save the uploaded file to a temporary location
        File tempFile = File.createTempFile("temp", null);
        file.transferTo(tempFile);

        // Upload the sliced video to Cloudinary and get the public URL
        String videoUrl = videoservice.uploadAndSliceVideo(tempFile);

        System.out.println(" ========================= operation done ===========================");

        // Clean up the temporary file
        tempFile.delete();

        return videoUrl;
    }
}
