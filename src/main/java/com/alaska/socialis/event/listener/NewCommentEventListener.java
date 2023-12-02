package com.alaska.socialis.event.listener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.NewCommentEvent;
import com.alaska.socialis.event.UserMentionInCommentEvent;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.model.User;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.services.ActivityService;
import com.alaska.socialis.services.NotificationService;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class NewCommentEventListener implements ApplicationListener<NewCommentEvent> {
    @Autowired
    private ActivityService activityService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onApplicationEvent(NewCommentEvent event) {
        this.checkMentionsInPost(event.getComment());
        this.createAndSendNotification(event);
        // ! save an activity
    }

    public void checkMentionsInPost(Comment comment) {
        List<String> mentions = this.activityService.extractUsernameFromPost(comment.getContent());

        if (mentions.size() > 0) {
            this.eventPublisher
                    .publishEvent(new UserMentionInCommentEvent(mentions, NotificationActivityType.MENTION, comment));
        }
    }

    public void createAndSendNotification(NewCommentEvent event) {
        User user = event.getComment().getPost().getUser();
        User source = event.getComment().getUser();

        Notification notificationObj = new Notification();
        notificationObj.setUser(user);
        notificationObj.setActivityType(NotificationActivityType.COMMENTED);
        notificationObj.setSource(source);
        notificationObj.setTargetId(event.getComment().getId());
        notificationObj.setTargetType(NotificationTargetType.COMMENT);
        notificationObj.setRead(false);

        Notification newNotification = this.notificationRepository.save(notificationObj);

        this.publishAlertToClient(newNotification);
    }

    public void publishAlertToClient(Notification notification) {
        this.notificationService.publishAlertToClient(notification);
    }
}
