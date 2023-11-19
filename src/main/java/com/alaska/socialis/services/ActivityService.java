package com.alaska.socialis.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Activity;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.ActivityDto;
import com.alaska.socialis.repository.ActivityRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.ActivityServiceInterface;
import com.alaska.socialis.utils.ActionType;
import com.alaska.socialis.utils.GroupType;

@Service
public class ActivityService implements ActivityServiceInterface {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    public List<ActivityDto> fetchAllUserActivities(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        List<Activity> allActivities = this.activityRepository.findAllByUserId(userId);

        List<ActivityDto> allActivitiesDto = new ArrayList<>();

        allActivitiesDto = allActivities.stream().map((activity) -> this.buildActivityDto(activity))
                .collect(Collectors.toList());

        return allActivitiesDto;
    }

    public List<String> extractUsernameFromPost(String content) {
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

    public ActivityDto buildActivityDto(Activity activity) {
        ActivityDto activityDto = new ActivityDto();
        activityDto.setId(activity.getId());
        activityDto.setCreatedAt(activity.getCreatedAt());
        Map<String, Object> targetData = this.buildTargetData(activity);
        Map<String, Object> activityLink = this.buildActivityLink(activity);

        activityDto.setTargetData(targetData.get("targetData"));
        activityDto.setGroupType(targetData.get("groupType"));

        activityDto.setActivityLink(activityLink.get("activityLink"));
        activityDto.setActionType(activityLink.get("actionType"));
        ;

        return activityDto;
    }

    public Map<String, Object> buildTargetData(Activity activity) {
        Map<String, Object> data = new HashMap<>();

        switch (activity.getGroupType()) {
            case POST:
                Map<String, Object> targetData = new HashMap<>();
                Post post = getPostActivity(activity.getTargetId());

                targetData.put("content", post.getContent());
                targetData.put("image", post.getPostImages().size() > 0 ? post.getPostImages().get(0) : null);
                data.put("groupType", GroupType.POST.getValue());
                data.put("targetData", targetData);

                break;

            default:
                break;
        }

        return data;
    }

    public Map<String, Object> buildActivityLink(Activity activity) {
        switch (activity.getActionType()) {
            case NEW_POST:
                Map<String, Object> data = new HashMap<>();
                User user = activity.getUser();
                Post existpost = getPostActivity(activity.getTargetId());
                data.put("activityLink", "/" + user.getUsername() + "/details/" + existpost.getUid());
                data.put("actionType", ActionType.NEW_POST.getValue());
                return data;

            default:
                return null;
        }
    }

    public Post getPostActivity(Long postId) {
        return this.postRepository.findById(postId).get();
    }
}
