package com.alaska.socialis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaska.socialis.model.Activity;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.ActivityDto;
import com.alaska.socialis.repository.ActivityRepository;
import com.alaska.socialis.utils.ActionType;
import com.alaska.socialis.utils.GroupType;

@Service
public class ActivityService {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Autowired
    private ActivityRepository activityRepository;

    private List<String> extractUsernameFromPost(String content) {
        List<String> mentions = new ArrayList<>();

        // Define the regular expression pattern for mentions
        Pattern mentionPattern = Pattern.compile("\\B@(\\w+)");

        // Create a Matcher object for the input post
        Matcher matcher = mentionPattern.matcher(content);

        // Find mentions and add them to the list
        while (matcher.find()) {
            String mention = matcher.group(1); // Capture the username without the '@'
            mentions.add(mention);
        }

        return mentions;
    }
}
