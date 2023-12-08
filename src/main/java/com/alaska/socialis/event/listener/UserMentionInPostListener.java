package com.alaska.socialis.event.listener;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.UserMentionInPostEvent;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.User;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.NotificationService;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class UserMentionInPostListener implements ApplicationListener<UserMentionInPostEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onApplicationEvent(UserMentionInPostEvent event) {
        List<String> mentions = event.getMentions();
        NotificationActivityType activityType = event.getActivityType();
        Post post = event.getPost();

        for (String mention : mentions) {
            User user = (User) userRepository.findByUsername(mention);
            System.out.println(mention);
            System.out.println(user.getId());
            if (Objects.nonNull(user)) {
                Notification notificationObj = new Notification();
                notificationObj.setUser(user);
                notificationObj.setActivityType(activityType);
                notificationObj.setSource(post.getUser());
                notificationObj.setTargetId(post.getId());
                notificationObj.setTargetType(NotificationTargetType.POST);
                notificationObj.setRead(false);
                notificationObj.setReadAt(null);

                Notification newNotification = this.notificationRepository.save(notificationObj);

                this.publishAlertToClient(newNotification);
            }
        }
    }

    public void publishAlertToClient(Notification notification) {
        this.notificationService.publishAlertToClient(notification);
    }

}
