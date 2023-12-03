package com.alaska.socialis.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.UserFollowEvent;
import com.alaska.socialis.model.Activity;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.User;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.services.NotificationService;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class UserFollowEventListener implements ApplicationListener<UserFollowEvent> {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onApplicationEvent(UserFollowEvent event) {
        User follower = event.getFollower();
        User following = event.getFollowing();

        Notification notificationObj = new Notification();
        notificationObj.setUser(following);
        notificationObj.setActivityType(NotificationActivityType.FOLLOWS);
        notificationObj.setSource(follower);
        notificationObj.setTargetId(following.getId());
        notificationObj.setTargetType(NotificationTargetType.USER);
        notificationObj.setRead(false);

        Notification newNotification = this.notificationRepository.save(notificationObj);
        this.publishAlertToClient(newNotification);
        // this.publishActivity(savedActivity, user, post);
    }

    public void publishAlertToClient(Notification notification) {
        this.notificationService.publishAlertToClient(notification);
    }

    public void publishActivity(Activity activity, User user, Post post) {

    }
}
