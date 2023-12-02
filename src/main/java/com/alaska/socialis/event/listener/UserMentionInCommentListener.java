package com.alaska.socialis.event.listener;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.UserMentionInCommentEvent;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.model.User;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.NotificationService;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class UserMentionInCommentListener implements ApplicationListener<UserMentionInCommentEvent> {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onApplicationEvent(UserMentionInCommentEvent event) {
        List<String> mentions = event.getMentions();
        NotificationActivityType activityType = event.getActivityType();
        Comment comment = event.getComment();

        for (String mention : mentions) {
            User user = (User) userRepository.findByUsername(mention);

            if (Objects.nonNull(user)) {
                Notification notificationObj = new Notification();
                notificationObj.setUser(user);
                notificationObj.setActivityType(activityType);
                notificationObj.setSource(comment.getUser());
                notificationObj.setTargetId(comment.getId());
                notificationObj.setTargetType(NotificationTargetType.COMMENT);
                notificationObj.setRead(false);

                Notification newNotification = this.notificationRepository.save(notificationObj);

                this.publishAlertToClient(newNotification);
            }
        }
    }

    public void publishAlertToClient(Notification notification) {
        this.notificationService.publishAlertToClient(notification);
    }
}
