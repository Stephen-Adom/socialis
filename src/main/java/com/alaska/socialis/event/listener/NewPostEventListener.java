package com.alaska.socialis.event.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.NewPostEvent;
import com.alaska.socialis.event.UserMentionInPostEvent;
import com.alaska.socialis.model.Activity;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.ActivityDto;
import com.alaska.socialis.repository.ActivityRepository;
import com.alaska.socialis.services.ActivityService;
import com.alaska.socialis.utils.ActionType;
import com.alaska.socialis.utils.GroupType;
import com.alaska.socialis.utils.NotificationActivityType;

@Component
public class NewPostEventListener implements ApplicationListener<NewPostEvent> {

    private static final String NEW_ACTIVITY_FEED = "/feed/activities/new";

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void onApplicationEvent(NewPostEvent event) {
        this.checkMentionsInPost(event.getPost());
        this.savePostActivity(event.getPost(), event.getUser());
    }

    public void checkMentionsInPost(Post post) {
        List<String> mentions = this.activityService.extractUsernameFromPost(post.getContent());

        if (mentions.size() > 0) {
            this.eventPublisher
                    .publishEvent(new UserMentionInPostEvent(mentions, NotificationActivityType.MENTION, post));
        }
    }

    public void savePostActivity(Post post, User user) {
        Activity newPostActivty = new Activity();
        newPostActivty.setUser(user);
        newPostActivty.setActionType(ActionType.NEW_POST);
        newPostActivty.setGroupType(GroupType.POST);
        newPostActivty.setTargetId(post.getId());

        Activity savedActivity = this.activityRepository.save(newPostActivty);
        this.publishActivity(savedActivity, user, post);
    }

    public void publishActivity(Activity activity, User user, Post post) {
        ActivityDto activityDto = new ActivityDto();
        activityDto.setId(activity.getId());
        activityDto.setCreatedAt(activity.getCreatedAt());
        activityDto.setActionType("NEW_POST");
        activityDto.setGroupType("POST");
        activityDto.setActivityLink("/" + user.getUsername() + "/details/" + post.getUid());

        Map<String, Object> targetData = new HashMap<String, Object>();
        targetData.put("content", post.getContent());
        if (post.getPostImages().size() > 0) {
            targetData.put("image", post.getPostImages().get(0));
        } else {
            targetData.put("image", null);
        }
        activityDto.setTargetData(targetData);

        messagingTemplate.convertAndSend(NEW_ACTIVITY_FEED + "-" + user.getUsername(), activityDto);
    }
}
