package com.alaska.socialis.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.services.VideoService;

@RestController
@RequestMapping("/api/stories")
public class StoriesController {

    @Autowired
    private VideoService videoservice;

    @PostMapping(value = "/{userId}/upload", headers = "Content-Type=multipart/form-data")
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
