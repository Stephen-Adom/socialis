package com.alaska.socialis.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class VideoService {
    @Autowired
    private Cloudinary cloudinary;

    @Value("${app.video-folder}")
    private String videoFolder;

    public String uploadAndSliceVideo(File videoFile) throws IOException {
        // Step 1: Use FFmpeg to slice the video
        String slicedVideoPath = videoFolder + "/output.mp4";

        String ffmpegCommand = "ffmpeg -i " + videoFile.getAbsolutePath() + " -ss 0 -t 60 -c copy " + slicedVideoPath;

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

        // Log the exit value
        System.out.println("FFmpeg process exit value: " + exitValue);

        // Check if FFmpeg completed successfully
        if (exitValue != 0) {
            throw new IOException("FFmpeg process did not complete successfully");
        }

        // Step 2: Upload the sliced video to Cloudinary
        Map<String, String> params = ObjectUtils.asMap(
                "resource_type", "video",
                "folder", "socialis/user/stories");

        Map<?, ?> uploadResult = cloudinary.uploader().upload(slicedVideoPath, params);

        // Clean up: Delete the temporary sliced video file
        File slicedVideoFile = new File(slicedVideoPath);
        if (slicedVideoFile.exists()) {
            slicedVideoFile.delete();
        }

        // Return the public URL of the uploaded video
        return (String) uploadResult.get("url");
    }
}
