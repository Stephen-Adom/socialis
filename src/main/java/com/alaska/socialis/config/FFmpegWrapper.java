package com.alaska.socialis.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class FFmpegWrapper {
    public static byte[] trim(byte[] videoData, double startTime, double endTime) {
        Objects.requireNonNull(videoData, "Video data must not be null");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y", // Overwrite output file if it exists
                "-i", "-", // Input from stdin
                "-ss", String.valueOf(startTime),
                "-to", String.valueOf(endTime),
                "-c", "copy",
                "-f", "mp4", // Output format
                "-" // Output to stdout
        );

        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        try (InputStream inputStream = new ByteArrayInputStream(videoData);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Process process = processBuilder.start();

            // Separate thread for writing to the input stream of the FFmpeg process
            Thread writeThread = new Thread(() -> {
                try (OutputStream processOutputStream = process.getOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        processOutputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Handle or log the exception as needed
                } finally {
                    try {
                        process.getOutputStream().close();
                    } catch (IOException e) {
                        e.printStackTrace(); // Handle or log the exception as needed
                    }
                }
            });

            // Separate thread for reading from the output stream of the FFmpeg process
            Thread readThread = new Thread(() -> {
                try (InputStream processInputStream = process.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = processInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Handle or log the exception as needed
                }
            });

            writeThread.start();
            readThread.start();

            // Wait for both threads to finish
            writeThread.join();
            readThread.join();

            // Check the exit code of the FFmpeg process
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return outputStream.toByteArray();
            } else {
                throw new IOException("FFmpeg process exited with non-zero status: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during video trimming " + e.getMessage());
        }
    }
}
