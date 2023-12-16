package com.alaska.socialis.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.KeyGenerator;

public class FFmpegUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegUtils.class);

    // Cross-platform line breaks
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Generate a random 16-byte AESKEY
     * 
     * @return
     */
    private static byte[] genAesKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Generate a key_info key file in the specified directory and return the
     * key_info file
     * 
     * @param folder
     * @throws IOException
     */
    private static Path genKeyInfo(String folder) throws IOException {
        // AES Keys
        byte[] aesKey = genAesKey();
        // AES vector
        String iv = Hex.encodeHexString(genAesKey());

        // key File write
        Path keyFile = Paths.get(folder, "key");
        Files.write(keyFile, aesKey, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // key_info File write
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("key").append(LINE_SEPARATOR); // m3u8 loads the network path of the key file
        stringBuilder.append(keyFile.toString()).append(LINE_SEPARATOR); // FFmeg loads key_info file path
        stringBuilder.append(iv); // ASE vector

        Path keyInfo = Paths.get(folder, "key_info");

        Files.write(keyInfo, stringBuilder.toString().getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        return keyInfo;
    }

    /**
     * Generate a master index.m3u8 file in the specified directory
     * 
     * @param fileName  master m3u8 file address
     * @param indexPath Access the path to the subindex.m3u8
     * @param bandWidth Streaming bitrate
     * @throws IOException
     */
    private static void genIndex(String file, String indexPath, String bandWidth) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#EXTM3U").append(LINE_SEPARATOR);
        stringBuilder.append("#EXT-X-STREAM-INF:BANDWIDTH=" + bandWidth).append(LINE_SEPARATOR); // 码率
        stringBuilder.append(indexPath);
        Files.write(Paths.get(file), stringBuilder.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * The transcoded video is m3u8
     * 
     * @param source     Source video
     * @param destFolder Destination folder
     * @param config     Configuration information
     * @throws IOException
     * @throws InterruptedException
     */
    public static void transcodeToM3u8(String source, String destFolder, TranscodeConfig config)
            throws IOException, InterruptedException {

        // Check whether the source video exists
        if (!Files.exists(Paths.get(source))) {
            throw new IllegalArgumentException("The file does not exist: " + source);
        }

        // Create a working directory
        Path workDir = Paths.get(destFolder, "ts");
        Files.createDirectories(workDir);

        // Generate a KeyInfo file in the working directory
        Path keyInfo = genKeyInfo(workDir.toString());

        // Build commands
        List<String> commands = new ArrayList<>();
        commands.add("ffmpeg");
        commands.add("-i");
        commands.add(source); // Source files
        commands.add("-c:v");
        commands.add("libx264"); // The video is encoded as H264
        commands.add("-c:a");
        commands.add("copy"); // Audio is copied directly
        commands.add("-hls_key_info_file");
        commands.add(keyInfo.toString()); // Specify the key file path
        commands.add("-hls_time");
        commands.add(config.getTsSeconds()); // TS slice size
        commands.add("-hls_playlist_type");
        commands.add("vod"); // On-demand mode
        commands.add("-hls_segment_filename");
        commands.add("%06d.ts"); // ts slice file name

        if (StringUtils.hasText(config.getCutStart())) {
            commands.add("-ss");
            commands.add(config.getCutStart()); // Start time
        }
        if (StringUtils.hasText(config.getCutEnd())) {
            commands.add("-to");
            commands.add(config.getCutEnd()); // End time
        }
        commands.add("index.m3u8"); // Generate an m3u8 file

        // Build the process
        Process process = new ProcessBuilder()
                .command(commands)
                .directory(workDir.toFile())
                .start();

        // Read process standard output
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    LOGGER.info(line);
                }
            } catch (IOException e) {
            }
        }).start();

        // Read the output of the process exception
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    LOGGER.info(line);
                }
            } catch (IOException e) {
            }
        }).start();

        // Blocks until the end of the mission
        if (process.waitFor() != 0) {
            throw new RuntimeException("The video slice is abnormal");
        }

        // Cut out the cover
        if (!screenShots(source, String.join(File.separator, destFolder, "poster.jpg"), config.getPoster())) {
            throw new RuntimeException("Cover cut anomalies");
        }

        // Get video information
        MediaInfo mediaInfo = getMediaInfo(source);
        if (mediaInfo == null) {
            throw new RuntimeException("The media information obtained is abnormal");
        }

        // Generate an index.m3u8 file
        genIndex(String.join(File.separator, destFolder, "index.m3u8"), "ts/index.m3u8",
                mediaInfo.getFormat().getBitRate());

        // Delete the keyInfo file
        Files.delete(keyInfo);
    }

    /**
     * Get the media information of a video file
     * 
     * @param source
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static MediaInfo getMediaInfo(String source) throws IOException, InterruptedException {
        List<String> commands = new ArrayList<>();
        commands.add("ffprobe");
        commands.add("-i");
        commands.add(source);
        commands.add("-show_format");
        commands.add("-show_streams");
        commands.add("-print_format");
        commands.add("json");

        Process process = new ProcessBuilder(commands)
                .start();

        MediaInfo mediaInfo = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            mediaInfo = new Gson().fromJson(bufferedReader, MediaInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (process.waitFor() != 0) {
            return null;
        }

        return mediaInfo;
    }

    /**
     * Captures a specified time frame of a video to generate an image file
     * 
     * @param source Source files
     * @param file   Image files
     * @param time   Screenshot time: HH:mm:ss. [SSS]
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean screenShots(String source, String file, String time)
            throws IOException, InterruptedException {

        List<String> commands = new ArrayList<>();
        commands.add("ffmpeg");
        commands.add("-i");
        commands.add(source);
        commands.add("-ss");
        commands.add(time);
        commands.add("-y");
        commands.add("-q:v");
        commands.add("1");
        commands.add("-frames:v");
        commands.add("1");
        commands.add("-f");
        ;
        commands.add("image2");
        commands.add(file);

        Process process = new ProcessBuilder(commands)
                .start();

        // Read process standard output
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    LOGGER.info(line);
                }
            } catch (IOException e) {
            }
        }).start();

        // Read the output of the process exception
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    LOGGER.error(line);
                }
            } catch (IOException e) {
            }
        }).start();

        return process.waitFor() == 0;
    }
}
